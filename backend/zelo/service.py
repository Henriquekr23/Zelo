import re
from datetime import date, timedelta

from fastapi import HTTPException
from sqlalchemy import select

from .models import Audit, Clinic, Record, Reminder, Ticket, Communication, now
from .schemas import SCHEMAS


def audit(session, clinic_id, actor, action, record_id, **detail):
    session.add(Audit(clinic_id=clinic_id, actor=actor, action=action,
                      record_id=record_id, detail=detail))


def get_record(session, clinic_id, record_id, kind):
    row = session.get(Record, str(record_id))
    if not row or row.clinic_id != clinic_id or row.kind != kind:
        raise HTTPException(404, "Record not found")
    return row


def clinic_lock(session, clinic_id):
    row = session.scalar(select(Clinic).where(Clinic.id == clinic_id).with_for_update())
    if not row:
        raise HTTPException(404, "Clinic not found")
    return row


def serialize(row):
    return {"id": row.id, "kind": row.kind, "version": row.version, "data": row.data}


def mutate(session, principal, mutation):
    clinic_lock(session, principal.clinic_id)
    existing = session.get(Record, str(mutation.id))
    if existing and (existing.clinic_id != principal.clinic_id or existing.kind != mutation.kind):
        raise HTTPException(404, "Record not found")
    data = SCHEMAS[mutation.kind].model_validate(mutation.data).model_dump(mode="json")
    # Replaying an accepted outbox operation is safe after a lost HTTP response.
    if existing and data == existing.data:
        return serialize(existing)
    if mutation.base_version != (existing.version if existing else 0):
        raise HTTPException(409, detail={"message": "Version conflict",
                                        "server": serialize(existing) if existing else None})
    if mutation.kind == "tutor":
        if data["consent"] and (not data["phone"] or not data["consent_source"].strip()):
            raise HTTPException(422, "Consent requires phone and evidence source")
        if data["phone"]:
            peers = session.scalars(select(Record).where(
                Record.clinic_id == principal.clinic_id, Record.kind == "tutor")).all()
            if any(p.id != str(mutation.id) and p.data["phone"] == data["phone"] for p in peers):
                raise HTTPException(409, "Phone already assigned to another tutor")
    else:
        tutor = get_record(session, principal.clinic_id, data["tutor_id"], "tutor")
        if not tutor.data["active"]:
            raise HTTPException(422, "An active tutor is required")
        if mutation.kind != "pet":
            pet = get_record(session, principal.clinic_id, data["pet_id"], "pet")
            if pet.data["tutor_id"] != tutor.id:
                raise HTTPException(422, "Pet and authorized tutor do not match")
        if mutation.kind == "procedure":
            procedures = session.scalars(select(Record).where(
                Record.clinic_id == principal.clinic_id, Record.kind == "procedure")).all()
            if any(p.id != str(mutation.id)
                   and p.data["source_appointment_id"] == data["source_appointment_id"] for p in procedures):
                raise HTTPException(409, "This clinical occurrence already has a recurrence")
            source = get_record(session, principal.clinic_id, data["source_appointment_id"], "appointment")
            if (source.data["pet_id"] != data["pet_id"] or source.data["status"] != "CONCLUIDO"
                    or source.data["tutor_id"] != tutor.id or source.data["date"] != data["performed_on"]):
                raise HTTPException(422, "Clinical source must be a completed appointment for this pet")
            if data["appointment_id"]:
                linked = get_record(session, principal.clinic_id, data["appointment_id"], "appointment")
                expected_due = (date.fromisoformat(data["performed_on"]) + timedelta(days=data["interval_days"])).isoformat() if data["interval_days"] else None
                if (linked.data["pet_id"] != data["pet_id"] or linked.data["tutor_id"] != tutor.id
                        or linked.data["status"] != "AGENDADO"
                        or (expected_due and linked.data["date"] != expected_due)):
                    raise HTTPException(422, "Follow-up appointment association invalid")
            if existing:
                raise HTTPException(409, "Clinical procedures are immutable; record a new occurrence")
        if mutation.kind == "appointment":
            if existing and existing.data["status"] != "AGENDADO" and data != existing.data:
                raise HTTPException(409, "Closed appointment is immutable")
            if data["status"] == "AGENDADO":
                appointments = session.scalars(select(Record).where(
                    Record.clinic_id == principal.clinic_id, Record.kind == "appointment")).all()
                if any(a.id != str(mutation.id) and a.data["status"] == "AGENDADO"
                       and a.data["date"] == data["date"] and a.data["time"] == data["time"]
                       for a in appointments):
                    raise HTTPException(409, "Appointment slot unavailable")
            # An attendance confirmation is changed by signed WhatsApp events, never by the mobile cache.
            if (not existing and data["attendance"] != "pending") or (
                existing and data["attendance"] != existing.data["attendance"]
            ):
                raise HTTPException(422, "Attendance is server managed")
    if mutation.kind == "tutor":
        audit(session, principal.clinic_id, principal.actor, "consent_recorded", str(mutation.id),
              consent=data["consent"], enabled=data["reminders_enabled"],
              source=data["consent_source"])
    if existing:
        existing.data = data
        existing.version += 1
        existing.updated_at = now()
        audit(session, principal.clinic_id, principal.actor, "record_updated", existing.id,
              previous_version=existing.version - 1)
    else:
        existing = Record(id=str(mutation.id), clinic_id=principal.clinic_id,
                          kind=mutation.kind, data=data, version=1)
        session.add(existing)
        audit(session, principal.clinic_id, principal.actor, "record_created", existing.id)
    session.flush()
    return serialize(existing)


def due_on(procedure):
    interval = procedure.data.get("interval_days")
    if not isinstance(interval, int) or isinstance(interval, bool) or not 1 <= interval <= 3650:
        return None
    return date.fromisoformat(procedure.data["performed_on"]) + timedelta(days=interval)


def eligible(session, reminder):
    procedure = get_record(session, reminder.clinic_id, reminder.procedure_id, "procedure")
    tutor = get_record(session, reminder.clinic_id, reminder.tutor_id, "tutor")
    pet = get_record(session, reminder.clinic_id, reminder.pet_id, "pet")
    valid = (
        due_on(procedure) == reminder.due_on
        and pet.data["tutor_id"] == tutor.id
        and procedure.data["tutor_id"] == tutor.id
        and tutor.data.get("active") and tutor.data.get("consent")
        and tutor.data.get("reminders_enabled", True)
        and re.fullmatch(r"\+[1-9][0-9]{7,14}", tutor.data.get("phone", ""))
    )
    return bool(valid), tutor, pet, procedure


def ticket(session, clinic_id, reminder_id, reason):
    session.add(Ticket(clinic_id=clinic_id, reminder_id=reminder_id, reason=reason))


def apply_reply(session, clinic, reminder, action):
    if reminder.response is not None:
        ticket(session, clinic.id, reminder.id, "conflicting_or_repeated_reply")
        return
    reminder.response = action
    if not reminder.appointment_id:
        ticket(session, clinic.id, reminder.id, "schedule_" + action)
        return
    try:
        appointment = get_record(session, clinic.id, reminder.appointment_id, "appointment")
    except HTTPException:
        ticket(session, clinic.id, reminder.id, "appointment_not_found")
        return
    if appointment.data["pet_id"] != reminder.pet_id or appointment.data["tutor_id"] != reminder.tutor_id:
        ticket(session, clinic.id, reminder.id, "invalid_appointment_association")
        return
    if appointment.data["status"] != "AGENDADO":
        ticket(session, clinic.id, reminder.id, "appointment_already_closed")
        return
    data = dict(appointment.data)
    data["attendance"] = {"confirm": "confirmed", "cancel": "cancelled",
                          "reschedule": "reschedule_requested"}[action]
    if action == "cancel":
        data["status"] = "CANCELADO"
    if action == "reschedule":
        ticket(session, clinic.id, reminder.id, "reschedule_requested")
    appointment.data = data
    appointment.version += 1
    appointment.updated_at = now()
    audit(session, clinic.id, "whatsapp", "appointment_" + action, appointment.id)
