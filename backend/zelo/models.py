import uuid
from datetime import datetime, timezone

from sqlalchemy import JSON, Boolean, Date, DateTime, ForeignKey, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column

from .db import Base


def uid():
    return str(uuid.uuid4())


def now():
    return datetime.now(timezone.utc).replace(tzinfo=None)


class Clinic(Base):
    __tablename__ = "clinics"
    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    timezone: Mapped[str] = mapped_column(String(80))
    lead_days: Mapped[int] = mapped_column(Integer)
    phone_number_id: Mapped[str] = mapped_column(String(80), unique=True)
    enabled: Mapped[bool] = mapped_column(Boolean, default=False)


class Record(Base):
    """Versioned server records. Payloads are strictly validated per kind at the boundary."""
    __tablename__ = "records"
    __table_args__ = (UniqueConstraint("clinic_id", "kind", "id"),)
    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    clinic_id: Mapped[str] = mapped_column(ForeignKey("clinics.id"), index=True)
    kind: Mapped[str] = mapped_column(String(20), index=True)
    version: Mapped[int] = mapped_column(Integer, default=1)
    data: Mapped[dict] = mapped_column(JSON)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=now)


class Reminder(Base):
    __tablename__ = "reminders"
    __table_args__ = (UniqueConstraint("clinic_id", "procedure_id", "due_on"),)
    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=uid)
    clinic_id: Mapped[str] = mapped_column(ForeignKey("clinics.id"), index=True)
    procedure_id: Mapped[str] = mapped_column(ForeignKey("records.id"))
    tutor_id: Mapped[str] = mapped_column(ForeignKey("records.id"))
    pet_id: Mapped[str] = mapped_column(ForeignKey("records.id"))
    appointment_id: Mapped[str | None] = mapped_column(String(36), nullable=True)
    due_on: Mapped[object] = mapped_column(Date)
    scheduled_on: Mapped[object] = mapped_column(Date)
    state: Mapped[str] = mapped_column(String(30), default="pending")
    attempts: Mapped[int] = mapped_column(Integer, default=0)
    next_attempt: Mapped[datetime] = mapped_column(DateTime, default=now)
    sent_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    claimed_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    provider_id: Mapped[str | None] = mapped_column(String(200), unique=True, nullable=True)
    error: Mapped[str | None] = mapped_column(String(80), nullable=True)
    response: Mapped[str | None] = mapped_column(String(30), nullable=True)


class Communication(Base):
    __tablename__ = "communications"
    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=uid)
    clinic_id: Mapped[str] = mapped_column(ForeignKey("clinics.id"), index=True)
    reminder_id: Mapped[str | None] = mapped_column(String(36), nullable=True)
    provider_id: Mapped[str | None] = mapped_column(String(200), nullable=True)
    sender: Mapped[str] = mapped_column(String(20), default="")
    direction: Mapped[str] = mapped_column(String(20))
    text: Mapped[str] = mapped_column(Text, default="")
    interpretation: Mapped[str] = mapped_column(String(50), default="")
    created_at: Mapped[datetime] = mapped_column(DateTime, default=now)


class Event(Base):
    __tablename__ = "webhook_events"
    key: Mapped[str] = mapped_column(String(64), primary_key=True)
    received_at: Mapped[datetime] = mapped_column(DateTime, default=now)


class Ticket(Base):
    __tablename__ = "tickets"
    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=uid)
    clinic_id: Mapped[str] = mapped_column(ForeignKey("clinics.id"), index=True)
    reminder_id: Mapped[str | None] = mapped_column(String(36), nullable=True)
    reason: Mapped[str] = mapped_column(String(80))
    state: Mapped[str] = mapped_column(String(20), default="open")
    resolution: Mapped[str] = mapped_column(Text, default="")
    created_at: Mapped[datetime] = mapped_column(DateTime, default=now)


class Audit(Base):
    __tablename__ = "audit"
    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=uid)
    clinic_id: Mapped[str] = mapped_column(String(36), index=True)
    actor: Mapped[str] = mapped_column(String(80))
    action: Mapped[str] = mapped_column(String(80))
    record_id: Mapped[str] = mapped_column(String(200))
    detail: Mapped[dict] = mapped_column(JSON, default=dict)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=now)
