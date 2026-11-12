import hashlib
import hmac
import json
from datetime import date, datetime, timedelta
from uuid import uuid4

import httpx
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, select
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from zelo import db
from zelo.api import app
from zelo.config import Settings, settings
from zelo.models import Base, Clinic, Record, Reminder, Communication, Ticket, Event
from zelo.provider import DeliveryError, MetaProvider
from zelo.worker import tick

TOKEN = "test-per-user-token-with-at-least-32-characters"
CLINIC = "11111111-1111-4111-8111-111111111111"
OTHER = "22222222-2222-4222-8222-222222222222"
TODAY = date.today()
INSTANT = datetime.combine(TODAY, datetime.min.time()).replace(hour=15)
PHONE = "+5511999999999"


class FakeProvider:
    def __init__(self, failure=None):
        self.calls = []
        self.failure = failure

    def send(self, *args):
        self.calls.append(args)
        if self.failure:
            raise self.failure
        return "wamid.test"


@pytest.fixture
def setup(monkeypatch):
    engine = create_engine("sqlite://", connect_args={"check_same_thread": False}, poolclass=StaticPool)
    Base.metadata.create_all(engine)
    monkeypatch.setattr(db, "Session", sessionmaker(engine, expire_on_commit=False))
    monkeypatch.setenv("META_APP_SECRET", "test-secret")
    monkeypatch.setenv("META_VERIFY_TOKEN", "verify-secret")
    monkeypatch.setenv("PRINCIPALS_JSON", json.dumps([{
        "clinic_id": CLINIC, "role": "admin",
        "token_sha256": hashlib.sha256(TOKEN.encode()).hexdigest(),
    }]))
    settings.cache_clear()
    with db.transaction() as session:
        session.add_all([
            Clinic(id=CLINIC, timezone="America/Sao_Paulo", lead_days=7, enabled=True, phone_number_id="phone"),
            Clinic(id=OTHER, timezone="America/Sao_Paulo", lead_days=7, enabled=True, phone_number_id="other"),
        ])
    client = TestClient(app)
    client.headers["Authorization"] = "Bearer " + TOKEN
    yield client
    settings.cache_clear()
    engine.dispose()


def put(client, kind, data, id=None, version=0):
    return client.put("/v1/records", json={"id": id or str(uuid4()), "kind": kind,
                                        "base_version": version, "data": data})


def seed(client, interval=30, consent=True):
    tutor = put(client, "tutor", {"name": "Carlos", "phone": PHONE,
                                 "consent": consent, "consent_source": "signed_form_v1"}).json()
    pet = put(client, "pet", {"name": "Thor", "tutor_id": tutor["id"]}).json()
    appt = {"pet_id": pet["id"], "tutor_id": tutor["id"], "date": (TODAY - timedelta(days=23)).isoformat(),
            "time": "10:00", "description": "Vacina", "status": "CONCLUIDO"}
    source = put(client, "appointment", appt).json()
    followup = put(client, "appointment", {**appt, "date": (TODAY - timedelta(days=23) + timedelta(days=interval or 30)).isoformat(), "status": "AGENDADO"}).json()
    procedure = put(client, "procedure", {
        "pet_id": pet["id"], "tutor_id": tutor["id"], "description": "Vacina",
        "performed_on": (TODAY - timedelta(days=23)).isoformat(), "interval_days": interval,
        "source_appointment_id": source["id"], "appointment_id": followup["id"],
    }).json()
    assert "id" in procedure, procedure
    return tutor, pet, procedure, followup


def snapshot():
    with db.transaction() as session:
        return session.scalars(select(Reminder)).all()


def webhook(client, message, sign=True, category="messages"):
    raw = json.dumps({"object": "whatsapp_business_account", "entry": [{"changes": [{
        "value": {"metadata": {"phone_number_id": "phone"}, category: [message]}}]}]}).encode()
    signature = "sha256=" + hmac.new(b"test-secret", raw, hashlib.sha256).hexdigest()
    return client.post("/webhooks/whatsapp", content=raw,
                       headers={"X-Hub-Signature-256": signature if sign else "invalid"})


def reply(reminder, action, id="inbound.1"):
    return {"id": id, "from": PHONE.lstrip("+"), "timestamp": "1794481200",
            "context": {"id": reminder.provider_id},
            "type": "button", "button": {"payload": f"zelo:{reminder.id}:{action}", "text": action}}


def test_recurring_send_and_dedup(setup):
    seed(setup)
    provider = FakeProvider()
    tick(provider=provider, instant=INSTANT)
    tick(provider=provider, instant=INSTANT)
    assert len(provider.calls) == 1
    reminder = snapshot()[0]
    assert reminder.due_on == TODAY + timedelta(days=7)
    assert reminder.scheduled_on == TODAY
    assert reminder.provider_id == "wamid.test"
    history = setup.get("/v1/communications").json()
    assert history["messages"][0]["direction"] == "outbound"


@pytest.mark.parametrize("interval,consent", [(None, True), (30, False), (90, True)])
def test_ineligible_not_sent(setup, interval, consent):
    seed(setup, interval, consent)
    provider = FakeProvider()
    tick(provider=provider, instant=INSTANT)
    assert provider.calls == []


@pytest.mark.parametrize("action,attendance,status", [
    ("confirm", "confirmed", "AGENDADO"), ("cancel", "cancelled", "CANCELADO"),
    ("reschedule", "reschedule_requested", "AGENDADO"),
])
def test_actions_and_duplicate_webhook(setup, action, attendance, status):
    *_, appt = seed(setup)
    tick(provider=FakeProvider(), instant=INSTANT)
    message = reply(snapshot()[0], action)
    assert webhook(setup, message).status_code == 200
    message["timestamp"] = "1794481201"
    assert webhook(setup, message).status_code == 200
    remote = next(r for r in setup.get("/v1/records").json() if r["id"] == appt["id"])
    assert remote["data"]["attendance"] == attendance
    assert remote["data"]["status"] == status
    assert remote["version"] == 2
    history = setup.get("/v1/communications").json()
    assert sum(m["direction"] == "inbound" for m in history["messages"]) == 1
    if action == "reschedule":
        assert history["tickets"][0]["reason"] == "reschedule_requested"


def test_signature_and_sender_protection(setup):
    *_, appt = seed(setup)
    tick(provider=FakeProvider(), instant=INSTANT)
    message = reply(snapshot()[0], "cancel")
    assert webhook(setup, message, sign=False).status_code == 401
    message["from"] = "5511888888888"
    webhook(setup, message)
    remote = next(r for r in setup.get("/v1/records").json() if r["id"] == appt["id"])
    assert remote["data"]["status"] == "AGENDADO"


def test_unknown_response_and_human_resolution(setup):
    seed(setup)
    tick(provider=FakeProvider(), instant=INSTANT)
    message = reply(snapshot()[0], "unknown")
    message["text"] = {"body": "Preciso de ajuda"}
    webhook(setup, message)
    history = setup.get("/v1/communications").json()
    assert history["messages"][-1]["interpretation"] == "human_required"
    ticket = history["tickets"][0]
    result = setup.post(f"/v1/tickets/{ticket['id']}/resolve", json={"note": "Tutor contatado pela recepção"})
    assert result.status_code == 200
    assert setup.get("/v1/communications").json()["tickets"][0]["state"] == "resolved"


def test_transient_backoff_and_persistent_failure(setup):
    seed(setup)
    config = Settings(max_attempts=2, retry_seconds=60)
    failing = FakeProvider(DeliveryError("meta_http_503", retryable=True))
    tick(config=config, provider=failing, instant=INSTANT)
    assert snapshot()[0].state == "retry"
    tick(config=config, provider=failing, instant=INSTANT + timedelta(seconds=30))
    assert len(failing.calls) == 1
    tick(config=config, provider=failing, instant=INSTANT + timedelta(seconds=61))
    assert snapshot()[0].state == "failed"
    assert setup.get("/v1/communications").json()["tickets"]


def test_retry_succeeds_within_window(setup):
    seed(setup)
    config = Settings(retry_seconds=60)
    tick(config=config, provider=FakeProvider(DeliveryError("connect", retryable=True)), instant=INSTANT)
    provider = FakeProvider()
    tick(config=config, provider=provider, instant=INSTANT + timedelta(seconds=61))
    assert snapshot()[0].state == "sent"
    assert snapshot()[0].attempts == 2


def test_uncertain_delivery_is_not_blindly_resent(setup):
    seed(setup)
    provider = FakeProvider(DeliveryError("timeout", uncertain=True))
    tick(provider=provider, instant=INSTANT)
    tick(provider=provider, instant=INSTANT + timedelta(hours=1))
    assert len(provider.calls) == 1
    assert snapshot()[0].state == "unknown"


def test_cross_clinic_and_nonexistent_associations(setup):
    tutor, *_ = seed(setup)
    assert put(setup, "pet", {"name": "Invalid", "tutor_id": str(uuid4())}).status_code == 404
    with db.transaction() as session:
        session.add(Record(id=str(uuid4()), clinic_id=OTHER, kind="tutor", version=1,
                           data={"name": "Other", "phone": PHONE, "active": True, "consent": True}))
        other = session.scalar(select(Record).where(Record.clinic_id == OTHER))
        other_id = other.id
    assert put(setup, "pet", {"name": "Invalid", "tutor_id": other_id}).status_code == 404
    assert all(r["id"] != other_id for r in setup.get("/v1/records").json())


@pytest.mark.parametrize("field,value", [("phone", "11999"), ("consent_source", "")])
def test_phone_and_consent_validation(setup, field, value):
    data = {"name": "Carlos", "phone": PHONE, "consent": True, "consent_source": "form"}
    data[field] = value
    assert put(setup, "tutor", data).status_code == 422


def test_inactive_tutor_and_revocation(setup):
    tutor, *_ = seed(setup)
    put(setup, "tutor", {**tutor["data"], "active": False}, tutor["id"], tutor["version"])
    assert put(setup, "pet", {"name": "Blocked", "tutor_id": tutor["id"]}).status_code == 422
    provider = FakeProvider()
    tick(provider=provider, instant=INSTANT)
    assert not provider.calls


def test_optimistic_conflict_and_replay(setup):
    tutor, *_ = seed(setup)
    new_data = {**tutor["data"], "name": "Updated"}
    accepted = put(setup, "tutor", new_data, tutor["id"], 1)
    assert accepted.status_code == 200
    assert put(setup, "tutor", new_data, tutor["id"], 1).json()["version"] == 2
    assert put(setup, "tutor", tutor["data"], tutor["id"], 1).status_code == 409


def test_no_auth_or_wrong_role(setup, monkeypatch):
    setup.headers.clear()
    assert setup.get("/v1/records").status_code == 401
    monkeypatch.setenv("PRINCIPALS_JSON", json.dumps([{
        "clinic_id": CLINIC, "role": "mobile", "token_sha256": hashlib.sha256(TOKEN.encode()).hexdigest(),
    }]))
    settings.cache_clear()
    setup.headers["Authorization"] = "Bearer " + TOKEN
    assert setup.get("/v1/communications").status_code == 403


def test_meta_template_contract(setup, monkeypatch):
    seed(setup)
    captured = {}
    def post(url, **kwargs):
        captured.update(url=url, **kwargs)
        return httpx.Response(200, json={"messages": [{"id": "wamid.test"}]})
    monkeypatch.setattr(httpx, "post", post)
    config = Settings(provider="meta", meta_access_token="test", meta_app_secret="test",
                      meta_verify_token="test", meta_phone_number_id="phone")
    tick(config=config, provider=MetaProvider(config), instant=INSTANT)
    assert captured["json"]["type"] == "template"
    assert len(captured["json"]["template"]["components"]) == 4
    assert captured["json"]["to"] == PHONE.lstrip("+")
    assert captured["headers"]["Authorization"] == "Bearer test"


def test_delivery_status_never_regresses(setup):
    seed(setup)
    tick(provider=FakeProvider(), instant=INSTANT)
    webhook(setup, {"id": "wamid.test", "status": "read", "timestamp": "1"}, category="statuses")
    webhook(setup, {"id": "wamid.test", "status": "sent", "timestamp": "2"}, category="statuses")
    assert snapshot()[0].state == "read"


def test_webhook_handshake(setup):
    assert setup.get("/webhooks/whatsapp?hub.mode=subscribe&hub.verify_token=verify-secret&hub.challenge=abc").text == "abc"
    assert setup.get("/webhooks/whatsapp?hub.mode=subscribe&hub.verify_token=wrong").status_code == 403


def test_duplicate_occurrence_and_slot_conflict(setup):
    _, _, procedure, appt = seed(setup)
    assert put(setup, "procedure", procedure["data"]).status_code == 409
    assert put(setup, "appointment", appt["data"]).status_code == 409


def test_invalid_recurrence(setup):
    _, _, procedure, _ = seed(setup)
    for interval in (0, -1, 3651):
        assert put(setup, "procedure", {**procedure["data"], "interval_days": interval}).status_code == 422


def test_missing_phone_and_disabled_tutor(setup):
    tutor, *_ = seed(setup)
    put(setup, "tutor", {**tutor["data"], "phone": "", "consent": False}, tutor["id"], 1)
    provider = FakeProvider()
    tick(provider=provider, instant=INSTANT)
    assert not provider.calls


def test_stop_revokes_consent_without_context(setup):
    tutor, *_ = seed(setup)
    message = {"id": "stop.1", "from": PHONE.lstrip("+"), "text": {"body": "PARAR"}}
    webhook(setup, message)
    remote = next(r for r in setup.get("/v1/records").json() if r["id"] == tutor["id"])
    assert remote["data"]["consent"] is False
    assert setup.get("/v1/communications").json()["messages"][0]["text"] == "PARAR"


def test_revocation_before_retry_suppresses_dispatch(setup):
    tutor, *_ = seed(setup)
    tick(provider=FakeProvider(DeliveryError("connect", retryable=True)), instant=INSTANT)
    put(setup, "tutor", {**tutor["data"], "consent": False}, tutor["id"], 1)
    provider = FakeProvider()
    tick(provider=provider, instant=INSTANT + timedelta(hours=1))
    assert not provider.calls
    assert snapshot()[0].state == "suppressed"


def test_missed_window_is_visible(setup):
    seed(setup)
    provider = FakeProvider()
    tick(provider=provider, instant=INSTANT + timedelta(days=8))
    assert not provider.calls
    assert setup.get("/v1/metrics").json()["missed_windows"] == 1


def test_sqlite_concurrent_workers(setup, monkeypatch, tmp_path):
    from concurrent.futures import ThreadPoolExecutor
    engine = create_engine("sqlite:///" + str(tmp_path / "concurrency.db"),
                           connect_args={"check_same_thread": False, "timeout": 30})
    Base.metadata.create_all(engine)
    monkeypatch.setattr(db, "Session", sessionmaker(engine, expire_on_commit=False))
    with db.transaction() as session:
        session.add(Clinic(id=CLINIC, timezone="America/Sao_Paulo", lead_days=7,
                           enabled=True, phone_number_id="phone"))
    seed(setup)
    provider = FakeProvider()
    with ThreadPoolExecutor(max_workers=2) as workers:
        futures = [workers.submit(tick, provider=provider, instant=INSTANT) for _ in range(2)]
        for future in futures:
            future.result()
    assert len(provider.calls) == 1
    assert len(snapshot()) == 1
    engine.dispose()


def test_missing_appointment_preserves_original_reply(setup):
    seed(setup)
    tick(provider=FakeProvider(), instant=INSTANT)
    reminder = snapshot()[0]
    with db.transaction() as session:
        session.get(Reminder, reminder.id).appointment_id = str(uuid4())
    assert webhook(setup, reply(reminder, "confirm")).status_code == 200
    history = setup.get("/v1/communications").json()
    assert history["tickets"][0]["reason"] == "appointment_not_found"
    assert history["messages"][-1]["text"] == "confirm"


def test_invalid_timezone_and_stale_creation(setup):
    assert setup.put("/v1/clinic", json={"enabled": False, "lead_days": 7,
                                       "timezone": "Invalid/Zone"}).status_code == 422
    _, _, _, appointment = seed(setup)
    assert put(setup, "appointment", appointment["data"], version=9).status_code == 409

