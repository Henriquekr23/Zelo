import hmac
import json

from fastapi import Depends, FastAPI, HTTPException, Request
from fastapi.responses import PlainTextResponse
from pydantic import ValidationError
from sqlalchemy import select

from .auth import authenticate, staff
from .config import settings
from .db import transaction
from .models import Audit, Clinic, Communication, Record, Reminder, Ticket
from .schemas import ClinicPreferences, Mutation, Resolution
from .service import TutorDeactivationBlocked, audit, clinic_lock, mutate, serialize
from .webhooks import process, valid_signature

app = FastAPI(title="Zelo WhatsApp integration", version="0.1.0")


@app.get("/health")
def health():
    return {"status": "ok", "provider": settings().provider}


@app.get("/v1/records")
def records(principal=Depends(authenticate)):
    staff(principal)
    with transaction() as session:
        return [serialize(r) for r in session.scalars(select(Record).where(
            Record.clinic_id == principal.clinic_id)).all()]


@app.put("/v1/records")
def put_record(body: Mutation, principal=Depends(authenticate)):
    staff(principal)
    try:
        with transaction() as session:
            return mutate(session, principal, body)
    except TutorDeactivationBlocked as failure:
        # The record transaction rolled back. Persist only the rejected decision,
        # never a partial tutor change, in a separate audit transaction.
        with transaction() as session:
            audit(session, principal.clinic_id, principal.actor, "tutor_deactivation_rejected",
                  failure.tutor_id, reason="active_responsibility_required")
        raise failure
    except ValidationError:
        raise HTTPException(422, "Invalid record data") from None


@app.get("/v1/clinic")
def get_clinic(principal=Depends(authenticate)):
    staff(principal)
    with transaction() as session:
        clinic = clinic_lock(session, principal.clinic_id)
        return {"id": clinic.id, "timezone": clinic.timezone, "lead_days": clinic.lead_days,
                "enabled": clinic.enabled, "provider_configured": settings().provider == "meta", "provider": settings().provider}


@app.put("/v1/clinic")
def preferences(body: ClinicPreferences, principal=Depends(authenticate)):
    if principal.role != "admin":
        raise HTTPException(403, "Admin permission required")
    with transaction() as session:
        clinic = clinic_lock(session, principal.clinic_id)
        if body.enabled and settings().provider == "disabled":
            raise HTTPException(409, "Configure Meta before enabling reminders")
        if body.enabled and settings().provider == "meta":
            clinic.phone_number_id = settings().meta_phone_number_id
        clinic.enabled, clinic.lead_days, clinic.timezone = body.enabled, body.lead_days, body.timezone
        audit(session, clinic.id, principal.actor, "clinic_preferences_updated", clinic.id)
    return {"ok": True}


def rows(session, model, clinic_id):
    return session.scalars(select(model).where(model.clinic_id == clinic_id)).all()


@app.get("/v1/communications")
def communications(principal=Depends(authenticate)):
    staff(principal)
    with transaction() as session:
        return {
            "reminders": [{"id": r.id, "procedure_id": r.procedure_id, "pet_id": r.pet_id,
                           "tutor_id": r.tutor_id, "appointment_id": r.appointment_id,
                           "due_on": r.due_on, "scheduled_on": r.scheduled_on,
                           "state": r.state, "attempts": r.attempts, "error": r.error,
                           "provider_id": r.provider_id, "sent_at": r.sent_at, "response": r.response}
                          for r in rows(session, Reminder, principal.clinic_id)],
            "messages": [{"id": m.id, "reminder_id": m.reminder_id, "direction": m.direction,
                          "text": m.text, "sender": m.sender, "provider_id": m.provider_id,
                          "interpretation": m.interpretation, "created_at": m.created_at}
                         for m in rows(session, Communication, principal.clinic_id)],
            "tickets": [{"id": t.id, "reminder_id": t.reminder_id, "reason": t.reason,
                         "state": t.state, "resolution": t.resolution}
                        for t in rows(session, Ticket, principal.clinic_id)],
        }


@app.post("/v1/tickets/{ticket_id}/resolve")
def resolve(ticket_id: str, body: Resolution, principal=Depends(authenticate)):
    staff(principal)
    with transaction() as session:
        clinic_lock(session, principal.clinic_id)
        row = session.get(Ticket, ticket_id)
        if not row or row.clinic_id != principal.clinic_id:
            raise HTTPException(404, "Ticket not found")
        row.state, row.resolution = "resolved", body.note
        audit(session, principal.clinic_id, principal.actor, "ticket_resolved", ticket_id, note=body.note)
    return {"ok": True}


@app.get("/v1/metrics")
def metrics(principal=Depends(authenticate)):
    staff(principal)
    with transaction() as session:
        reminders = rows(session, Reminder, principal.clinic_id)
        counts = {}
        for reminder in reminders:
            counts[reminder.state] = counts.get(reminder.state, 0) + 1
        return {"reminders_by_state": counts,
                "attempts": sum(r.attempts for r in reminders),
                "open_tickets": sum(t.state == "open" for t in rows(session, Ticket, principal.clinic_id)),
                "missed_windows": counts.get("missed", 0)}


@app.get("/v1/audit")
def audit_history(principal=Depends(authenticate)):
    staff(principal)
    with transaction() as session:
        return [{"actor": a.actor, "action": a.action, "record_id": a.record_id,
                 "detail": a.detail, "created_at": a.created_at}
                for a in rows(session, Audit, principal.clinic_id)]


@app.get("/webhooks/whatsapp", response_class=PlainTextResponse)
def verify(request: Request):
    query, config = request.query_params, settings()
    if (not config.meta_verify_token or query.get("hub.mode") != "subscribe"
            or not hmac.compare_digest(query.get("hub.verify_token", ""), config.meta_verify_token)):
        raise HTTPException(403, "Verification failed")
    return query.get("hub.challenge", "")


@app.post("/webhooks/whatsapp")
async def webhook(request: Request):
    length = request.headers.get("content-length", "0")
    if not length.isdigit():
        raise HTTPException(400, "Invalid content length")
    if int(length) > 1048576:
        raise HTTPException(413)
    buffer = bytearray()
    async for chunk in request.stream():
        buffer.extend(chunk)
        if len(buffer) > 1048576:
            raise HTTPException(413)
    raw = bytes(buffer)
    if not valid_signature(raw, request.headers.get("x-hub-signature-256"), settings().meta_app_secret):
        raise HTTPException(401, "Invalid webhook signature")
    try:
        payload = json.loads(raw)
        if not isinstance(payload, dict):
            raise ValueError()
    except ValueError:
        raise HTTPException(400, "Invalid payload") from None
    # Run transactional processing outside the ASGI event loop.
    from starlette.concurrency import run_in_threadpool
    await run_in_threadpool(process, payload)
    return {"ok": True}
