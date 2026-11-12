import json
import re
from functools import lru_cache

from pydantic import model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")
    database_url: str = "sqlite:///./data/zelo.db"
    principals_json: str = "[]"
    clinic_id: str = "11111111-1111-4111-8111-111111111111"
    clinic_timezone: str = "America/Sao_Paulo"
    lead_days: int = 7
    meta_phone_number_id: str = ""
    meta_access_token: str = ""
    meta_app_secret: str = ""
    meta_verify_token: str = ""
    graph_version: str = "v25.0"
    template_name: str = "zelo_cuidado_recorrente"
    template_language: str = "pt_BR"
    max_attempts: int = 5
    retry_seconds: int = 300
    poll_seconds: int = 60
    sending_timeout_seconds: int = 300
    provider: str = "disabled"

    @model_validator(mode="after")
    def validate_config(self):
        from zoneinfo import ZoneInfo
        from uuid import UUID
        ZoneInfo(self.clinic_timezone)
        UUID(self.clinic_id)
        if not 0 <= self.lead_days <= 90 or self.max_attempts < 1:
            raise ValueError("Invalid reminder settings")
        if self.poll_seconds < 1 or self.retry_seconds < 1:
            raise ValueError("Invalid worker interval")
        if not re.fullmatch(r"v[0-9]+\.0", self.graph_version):
            raise ValueError("Invalid Graph version")
        if self.provider not in ("disabled", "mock", "meta"):
            raise ValueError("Use disabled, mock or meta")
        if self.provider == "meta" and not all((
            self.meta_access_token, self.meta_app_secret,
            self.meta_verify_token, self.meta_phone_number_id,
        )):
            raise ValueError("Meta configuration incomplete")
        for principal in json.loads(self.principals_json):
            UUID(principal["clinic_id"])
            if principal["role"] not in ("admin", "reception", "mobile"):
                raise ValueError("Invalid role")
            if not re.fullmatch("[0-9a-f]{64}", principal["token_sha256"]):
                raise ValueError("Store SHA256 of a strong per-user token")
        return self


@lru_cache
def settings():
    return Settings()
