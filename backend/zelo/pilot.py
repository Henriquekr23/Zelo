"""Create explicit fictional pilot records in the SERVER SQLite only."""
from datetime import date, timedelta
from uuid import uuid5, NAMESPACE_URL

from .auth import Principal
from .bootstrap import main as bootstrap
from .config import settings
from .db import transaction
from .models import Record, Clinic
from .schemas import Mutation
from .service import mutate


def main():
    config = settings()
    if config.provider != "mock":
        raise SystemExit("Pilot fixture requires PROVIDER=mock. No real sends allowed.")
    bootstrap()
    clinic_id = config.clinic_id
    def key(kind):
        return str(uuid5(NAMESPACE_URL, clinic_id + ":fictional-pilot:" + kind))
    today = date.today()
    tutor, pet, source, followup, procedure = [key(k) for k in ("tutor", "pet", "source", "followup", "procedure")]
    records = [
        (tutor, "tutor", {"name": "Tutor de demonstração", "phone": "+5511999999999",
                         "consent": True, "consent_source": "fictional_mock_fixture"}),
        (pet, "pet", {"name": "Pet de demonstração", "tutor_id": tutor}),
        (source, "appointment", {"pet_id": pet, "tutor_id": tutor,
             "date": (today - timedelta(days=23)).isoformat(), "time": "10:00",
             "description": "Dose de demonstração", "status": "CONCLUIDO"}),
        (followup, "appointment", {"pet_id": pet, "tutor_id": tutor,
             "date": (today + timedelta(days=7)).isoformat(), "time": "10:00",
             "description": "Próxima dose de demonstração"}),
        (procedure, "procedure", {"pet_id": pet, "tutor_id": tutor,
             "performed_on": (today - timedelta(days=23)).isoformat(), "interval_days": 30,
             "description": "Dose de demonstração", "source_appointment_id": source,
             "appointment_id": followup}),
    ]
    with transaction() as session:
        principal = Principal(clinic_id, "admin", "local_pilot_fixture")
        for record_id, kind, data in records:
            if not session.get(Record, record_id):
                mutate(session, principal, Mutation(id=record_id, kind=kind, base_version=0, data=data))
        session.get(Clinic, clinic_id).enabled = True
    print("Fictional pilot prepared. Run python -m zelo.worker; provider is MOCK.")
    print("Procedure ID:", procedure)


if __name__ == "__main__":
    main()
