import json
import time
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo

from sqlalchemy import select

from .config import settings
from .db import transaction
from .models import Clinic, Record, Reminder, Communication, now
from .provider import DeliveryError, configured_provider
from .service import audit, due_on, eligible, prepare_delivery, ticket


def scan(session, clinic, instant):
    today = instant.replace(tzinfo=ZoneInfo("UTC")).astimezone(ZoneInfo(clinic.timezone)).date()
    procedures = session.scalars(select(Record).where(
        Record.clinic_id == clinic.id, Record.kind == "procedure")).all()
    # One occurrence per clinical record. A new administered dose creates the next occurrence.
    for procedure in procedures:
        due = due_on(procedure)
        if due is None or due > today + timedelta(days=clinic.lead_days):
            continue
        existing = session.scalar(select(Reminder).where(
            Reminder.clinic_id == clinic.id, Reminder.procedure_id == procedure.id,
            Reminder.due_on == due))
        if existing:
            continue
        reminder = Reminder(clinic_id=clinic.id, procedure_id=procedure.id,
                            tutor_id=procedure.data["tutor_id"], pet_id=procedure.data["pet_id"],
                            appointment_id=procedure.data["appointment_id"], due_on=due,
                            scheduled_on=due - timedelta(days=clinic.lead_days), next_attempt=instant)
        valid, *_ = eligible(session, reminder)
        if valid:
            if due < today:
                reminder.state = "missed"
            session.add(reminder)
            session.flush()
            kind = prepare_delivery(session, reminder)
            audit(session, clinic.id, "worker", "reminder_scheduled", reminder.id, kind=kind)
            if due < today:
                ticket(session, clinic.id, reminder.id, "processing_window_missed")


def tick(config=None, provider=None, instant=None):
    config = config or settings()
    provider = provider or configured_provider(config)
    instant = instant or now()
    # Clinic row locks serialize all writers for a tenant, including webhook and sync.
    with transaction() as session:
        clinics = session.scalars(select(Clinic).with_for_update(skip_locked=True)).all()
        for clinic in clinics:
            if clinic.enabled:
                scan(session, clinic, instant)
            stale = session.scalars(select(Reminder).where(
                Reminder.clinic_id == clinic.id, Reminder.state == "sending",
                Reminder.claimed_at < instant - timedelta(seconds=config.sending_timeout_seconds))).all()
            for reminder in stale:
                reminder.state, reminder.error = "unknown", "worker_interrupted"
                ticket(session, clinic.id, reminder.id, "delivery_unknown")
    while True:
        with transaction() as session:
            reminder = session.scalar(select(Reminder).join(Clinic).where(
                Clinic.enabled.is_(True), Reminder.state.in_(["pending", "retry"]),
                Reminder.next_attempt <= instant,
            ).order_by(Reminder.next_attempt).with_for_update(skip_locked=True).limit(1))
            if not reminder:
                break
            clinic = session.get(Clinic, reminder.clinic_id)
            valid, tutor, pet, procedure = eligible(session, reminder)
            today = instant.replace(tzinfo=ZoneInfo("UTC")).astimezone(ZoneInfo(clinic.timezone)).date()
            if not valid or today > reminder.due_on:
                reminder.state = "suppressed"
                audit(session, clinic.id, "worker", "reminder_suppressed", reminder.id)
                continue
            prepare_delivery(session, reminder)
            reminder.state, reminder.claimed_at = "sending", instant
            reminder.attempts += 1
            reminder_id = reminder.id
            audit(session, clinic.id, "worker", "send_attempt", reminder.id, attempt=reminder.attempts)
        # Sending is durably claimed before the network call. Crash => human reconciliation.
        # Recheck consent under the SQLite writer reservation. Revocation cannot
        # race a network dispatch; the short provider timeout bounds this pilot lock.
        with transaction() as session:
            reminder = session.get(Reminder, reminder_id)
            clinic = session.get(Clinic, reminder.clinic_id)
            valid, tutor, pet, procedure = eligible(session, reminder)
            if not valid or not clinic.enabled:
                reminder.state = "suppressed"
                continue
            prepare_delivery(session, reminder)
            try:
                provider_id = provider.send(clinic, reminder, tutor, pet, procedure)
                reminder.provider_id = provider_id
                failure = None
            except DeliveryError as exc:
                failure = exc
        with transaction() as session:
            row = session.scalar(select(Reminder).where(Reminder.id == reminder_id).with_for_update())
            if failure:
                row.error = failure.code
                if failure.uncertain:
                    row.state = "unknown"
                elif failure.retryable and row.attempts < config.max_attempts:
                    row.state = "retry"
                    row.next_attempt = instant + timedelta(
                        seconds=config.retry_seconds * 2 ** (row.attempts - 1))
                else:
                    row.state = "failed"
                if row.state in ("failed", "unknown"):
                    ticket(session, row.clinic_id, row.id, row.error)
                audit(session, row.clinic_id, "worker", row.state, row.id, code=row.error)
            else:
                row.provider_id, row.sent_at = provider_id, now()
                row.error = None
                if row.state == "sending":
                    row.state = "simulated" if getattr(provider, "simulated", False) else "sent"
                session.add(Communication(clinic_id=row.clinic_id, reminder_id=row.id,
                                           provider_id=provider_id, direction="outbound",
                                           text=json.dumps({
                                               "kind": "appointment_confirmation" if row.appointment_id else "clinical_due",
                                               "template": config.template_name if row.appointment_id else config.due_template_name,
                                               "language": config.template_language,
                                               "parameters": [tutor.data["name"], pet.data["name"],
                                                              procedure.data["description"],
                                                              row.due_on.strftime("%d/%m/%Y")],
                                           }, ensure_ascii=False),
                                           interpretation="simulated_template" if getattr(provider, "simulated", False)
                                           else "template_requested"))
                audit(session, row.clinic_id, "worker", "template_requested", row.id)


def main():
    config = settings()
    while True:
        try:
            tick(config)
        except Exception:
            # No payloads, credentials or phone numbers in operational logs.
            import logging
            logging.error("Worker cycle failed; inspect authenticated audit/metrics")
        time.sleep(config.poll_seconds)


if __name__ == "__main__":
    from .worker_lock import single_worker
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--once", action="store_true", help="Process one durable scheduler cycle")
    arguments = parser.parse_args()
    with single_worker(settings().database_url):
        if arguments.once:
            tick()
        else:
            main()
