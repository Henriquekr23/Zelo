"""Initialize a NEW server database. Never imports, deletes or modifies Android data."""
from sqlalchemy import text

from .config import settings
from .db import Base, engine, transaction
from .models import Clinic


def main():
    config = settings()
    with engine.connect() as connection:
        version = connection.scalar(text("PRAGMA user_version"))
    if version not in (0, 1):
        raise RuntimeError("Unsupported backend schema version")
    Base.metadata.create_all(engine)
    with engine.begin() as connection:
        connection.execute(text("PRAGMA user_version=1"))
    with transaction() as session:
        if not session.get(Clinic, config.clinic_id):
            session.add(Clinic(id=config.clinic_id, timezone=config.clinic_timezone,
                               lead_days=config.lead_days,
                               phone_number_id=config.meta_phone_number_id or "unconfigured",
                               enabled=False))
    print("Schema initialized; clinic reminders disabled until configured by admin.")


if __name__ == "__main__":
    main()
