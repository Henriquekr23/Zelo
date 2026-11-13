import hashlib
import hmac
import json
from dataclasses import dataclass

from fastapi import Header, HTTPException

from .config import settings


@dataclass
class Principal:
    clinic_id: str
    role: str
    actor: str


def authenticate(authorization: str = Header(default="")):
    scheme, _, token = authorization.partition(" ")
    digest = hashlib.sha256(token.encode()).hexdigest()
    if scheme.lower() == "bearer" and len(token) >= 32:
        for item in json.loads(settings().principals_json):
            if hmac.compare_digest(digest, item["token_sha256"]):
                return Principal(item["clinic_id"], item["role"], digest[:16])
    raise HTTPException(401, "Authentication required")


def staff(principal):
    if principal.role not in ("admin", "reception"):
        raise HTTPException(403, "Clinic staff permission required")
