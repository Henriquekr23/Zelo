import hashlib
import hmac

from sqlalchemy import select
from sqlalchemy.exc import IntegrityError

from .db import transaction
from .models import Clinic, Communication, Event, Reminder
from .service import apply_reply, audit, clinic_lock, eligible, ticket


def valid_signature(raw, signature, secret):
    if not secret:
        return False
    expected = "sha256=" + hmac.new(secret.encode(), raw, hashlib.sha256).hexdigest()
    return hmac.compare_digest(expected, signature or "")


def process(payload):
    if payload.get("object") != "whatsapp_business_account":
        return
    for entry in payload.get("entry", []):
        for change in entry.get("changes", []):
            value = change.get("value", {})
            phone_id = value.get("metadata", {}).get("phone_number_id")
            for category in ("messages", "statuses"):
                for item in value.get(category, []):
                    key = hashlib.sha256(
                        f"{phone_id}:{category}:{item.get('id')}:{item.get('status', '')}".encode()
                    ).hexdigest()
                    try:
                        with transaction() as session:
                            clinic = session.scalar(select(Clinic).where(Clinic.phone_number_id == phone_id))
                            if not clinic:
                                continue
                            clinic_lock(session, clinic.id)
                            if session.get(Event, key):
                                continue
                            session.add(Event(key=key))
                            session.flush()
                            if category == "statuses":
                                status(session, clinic, item)
                            else:
                                message(session, clinic, item)
                    except IntegrityError:
                        # Concurrent duplicate. The losing transaction has no effects.
                        continue


def status(session, clinic, item):
    reminder = session.scalar(select(Reminder).where(
        Reminder.clinic_id == clinic.id, Reminder.provider_id == item.get("id")))
    if not reminder and item.get("biz_opaque_callback_data"):
        reminder = session.scalar(select(Reminder).where(
            Reminder.clinic_id == clinic.id, Reminder.id == item["biz_opaque_callback_data"],
            Reminder.state.in_(["sending", "unknown"])))
        if reminder:
            reminder.provider_id = item["id"]
    if not reminder:
        return
    incoming = item.get("status")
    rank = {"sending": 0, "unknown": 0, "sent": 1, "delivered": 2, "read": 3}
    if incoming in ("sent", "delivered", "read") and rank.get(incoming, 0) > rank.get(reminder.state, 0):
        reminder.state = incoming
    elif incoming == "failed" and reminder.state not in ("delivered", "read"):
        reminder.state, reminder.error = "failed", "provider_delivery_failed"
        ticket(session, clinic.id, reminder.id, "provider_delivery_failed")
    audit(session, clinic.id, "meta", "delivery_status", reminder.id, status=incoming)


def message(session, clinic, item):
    context_id = item.get("context", {}).get("id")
    reminder = session.scalar(select(Reminder).where(
        Reminder.clinic_id == clinic.id, Reminder.provider_id == context_id)) if context_id else None
    sender = "+" + item.get("from", "").lstrip("+")
    if reminder:
        valid, tutor, *_ = eligible(session, reminder)
        if tutor.data["phone"] != sender:
            audit(session, clinic.id, "meta", "reply_sender_mismatch", reminder.id)
            reminder = None
            valid = False
    # Unknown context cannot select an appointment. Still preserve messages for staff.
    text = item.get("text", {}).get("body", "")
    if text.strip().casefold() in ("parar", "sair", "stop"):
        from .models import Record, now
        tutors = session.scalars(select(Record).where(
            Record.clinic_id == clinic.id, Record.kind == "tutor")).all()
        for recipient in tutors:
            if recipient.data.get("phone") == sender:
                recipient.data = {**recipient.data, "consent": False}
                recipient.version += 1
                recipient.updated_at = now()
                audit(session, clinic.id, "whatsapp", "consent_revoked", recipient.id)
        session.add(Communication(clinic_id=clinic.id, reminder_id=reminder.id if reminder else None,
                                  provider_id=item.get("id"), sender=sender, direction="inbound",
                                  text=text[:4096], interpretation="consent_revoked"))
        return
    button = item.get("button", {})
    interactive = item.get("interactive", {}).get("button_reply", {})
    payload = button.get("payload") or interactive.get("id") or ""
    original = text or button.get("text") or interactive.get("title") or "[unsupported message]"
    interpretation = "human_required"
    if reminder and valid and clinic.enabled:
        parts = payload.split(":")
        if len(parts) == 3 and parts[:2] == ["zelo", reminder.id] and parts[2] in ("confirm", "reschedule", "cancel"):
            interpretation = parts[2]
            apply_reply(session, clinic, reminder, interpretation)
    session.add(Communication(clinic_id=clinic.id, reminder_id=reminder.id if reminder else None,
                              provider_id=item.get("id"), sender=sender, direction="inbound",
                              text=original[:4096], interpretation=interpretation))
    if interpretation == "human_required":
        ticket(session, clinic.id, reminder.id if reminder else None, "unrecognized_response")
