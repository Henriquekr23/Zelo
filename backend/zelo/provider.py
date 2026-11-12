from typing import Protocol

import httpx


class DeliveryError(Exception):
    def __init__(self, code, retryable=False, uncertain=False):
        self.code, self.retryable, self.uncertain = code, retryable, uncertain


class Provider(Protocol):
    def send(self, clinic, reminder, tutor, pet, procedure) -> str: ...


class MetaProvider:
    def __init__(self, config):
        self.config = config

    def send(self, clinic, reminder, tutor, pet, procedure):
        c = self.config
        if c.provider != "meta" or clinic.phone_number_id != c.meta_phone_number_id:
            raise DeliveryError("provider_not_configured")
        components = [{
            "type": "body", "parameters": [
                {"type": "text", "text": value} for value in (
                    tutor.data["name"], pet.data["name"],
                    procedure.data["description"], reminder.due_on.strftime("%d/%m/%Y"),
                )
            ],
        }]
        for index, action in enumerate(("confirm", "reschedule", "cancel")):
            components.append({
                "type": "button", "sub_type": "quick_reply", "index": str(index),
                "parameters": [{"type": "payload", "payload": f"zelo:{reminder.id}:{action}"}],
            })
        body = {
            "messaging_product": "whatsapp",
            "to": tutor.data["phone"].lstrip("+"), "type": "template",
            "biz_opaque_callback_data": reminder.id,
            "template": {"name": c.template_name,
                         "language": {"code": c.template_language}, "components": components},
        }
        try:
            response = httpx.post(
                f"https://graph.facebook.com/{c.graph_version}/{clinic.phone_number_id}/messages",
                headers={"Authorization": f"Bearer {c.meta_access_token}"},
                json=body, timeout=httpx.Timeout(20, connect=5),
            )
        except (httpx.ConnectError, httpx.ConnectTimeout):
            raise DeliveryError("connection_unavailable", retryable=True) from None
        except httpx.TransportError:
            # Meta has no documented idempotency key for this operation. Never resend blindly.
            raise DeliveryError("delivery_unknown", uncertain=True) from None
        if response.status_code >= 400:
            raise DeliveryError(f"meta_http_{response.status_code}",
                                retryable=response.status_code == 429 or response.status_code >= 500)
        try:
            return response.json()["messages"][0]["id"]
        except (ValueError, KeyError, IndexError):
            raise DeliveryError("invalid_provider_response", uncertain=True) from None


class MockProvider:
    """Explicit local simulation; never contacts Meta."""
    simulated = True

    def send(self, clinic, reminder, tutor, pet, procedure):
        return "mock:" + reminder.id


def configured_provider(config):
    return MockProvider() if config.provider == "mock" else MetaProvider(config)
