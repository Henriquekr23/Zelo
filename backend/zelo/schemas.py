import re
from datetime import date, datetime
from typing import Literal
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field, field_validator


class Strict(BaseModel):
    model_config = ConfigDict(extra="forbid")


class Tutor(Strict):
    name: str = Field(min_length=1, max_length=120)
    phone: str = ""
    active: bool = True
    consent: bool = False
    reminders_enabled: bool = True
    consent_source: str = Field(default="", max_length=200)

    @field_validator("phone")
    @classmethod
    def phone_format(cls, value):
        if value and not re.fullmatch(r"\+[1-9][0-9]{7,14}", value):
            raise ValueError("Use E.164: +5511999999999")
        return value


class Pet(Strict):
    name: str = Field(min_length=1, max_length=120)
    tutor_id: UUID


class Appointment(Strict):
    pet_id: UUID
    tutor_id: UUID
    date: date
    time: str = Field(pattern=r"^([01][0-9]|2[0-3]):[0-5][0-9]$")
    description: str = Field(min_length=1, max_length=500)
    status: Literal["AGENDADO", "CONCLUIDO", "CANCELADO"] = "AGENDADO"
    attendance: Literal["pending", "confirmed", "reschedule_requested", "cancelled"] = "pending"


class Procedure(Strict):
    pet_id: UUID
    tutor_id: UUID
    description: str = Field(min_length=1, max_length=500)
    performed_on: date
    interval_days: int | None = Field(default=None, ge=1, le=3650)
    appointment_id: UUID | None = None
    source_appointment_id: UUID

    @field_validator("performed_on")
    @classmethod
    def not_future(cls, value):
        if value > date.today():
            raise ValueError("A clinical procedure must already have occurred")
        return value


class Mutation(Strict):
    id: UUID
    kind: Literal["tutor", "pet", "appointment", "procedure"]
    base_version: int = Field(ge=0)
    data: dict


class Resolution(Strict):
    note: str = Field(min_length=1, max_length=2000)


class ClinicPreferences(Strict):
    enabled: bool
    lead_days: int = Field(ge=0, le=90)
    timezone: str

    @field_validator("timezone")
    @classmethod
    def valid_zone(cls, value):
        from zoneinfo import ZoneInfo
        try:
            ZoneInfo(value)
        except (KeyError, ValueError):
            raise ValueError("Invalid clinic timezone") from None
        return value


SCHEMAS = {"tutor": Tutor, "pet": Pet, "appointment": Appointment, "procedure": Procedure}
