import json
from decimal import Decimal

import httpx
import pytest

from coperti_bot.api import ApiError, CopertiClient

BASE = "http://coperti.test/api"


def client_with(handler):
    requests = []

    def record(request: httpx.Request) -> httpx.Response:
        requests.append(request)
        return handler(request)

    return CopertiClient(BASE, "s3cret", transport=httpx.MockTransport(record)), requests


async def test_every_request_carries_the_bot_token():
    client, requests = client_with(lambda r: httpx.Response(200, json=[]))
    await client.warehouses()
    assert requests[0].headers["X-Bot-Token"] == "s3cret"
    assert "X-Telegram-User-Id" not in requests[0].headers


async def test_search_items_sends_query():
    client, requests = client_with(lambda r: httpx.Response(200, json=[{"id": "i1", "name": "Screw"}]))

    assert await client.search_items("scr") == [{"id": "i1", "name": "Screw"}]
    assert requests[0].url.path == "/api/item"
    assert requests[0].url.params["search"] == "scr"


async def test_me_identifies_the_telegram_user():
    client, requests = client_with(lambda r: httpx.Response(200, json={"id": "e1", "name": "Ann"}))
    assert (await client.me(42))["name"] == "Ann"
    assert requests[0].url.path == "/api/bot/me"
    assert requests[0].headers["X-Telegram-User-Id"] == "42"


async def test_link_sends_code():
    client, requests = client_with(lambda r: httpx.Response(200, json={"id": "e1", "name": "Ann"}))
    await client.link(42, "ABCD2345")
    assert requests[0].method == "POST" and requests[0].url.path == "/api/bot/link"
    assert json.loads(requests[0].content) == {"code": "ABCD2345"}


async def test_receive_goes_through_bot_endpoint_without_performer():
    client, requests = client_with(lambda r: httpx.Response(200, json={"id": "m1"}))
    await client.receive(42, "i1", "w1", Decimal("2.5"))

    assert requests[0].method == "POST" and requests[0].url.path == "/api/bot/receive"
    assert requests[0].headers["X-Telegram-User-Id"] == "42"
    # who performed it is the backend's decision, never the bot's
    assert json.loads(requests[0].content) == {"itemId": "i1", "warehouseId": "w1", "qty": 2.5}


async def test_give_sends_recipient():
    client, requests = client_with(lambda r: httpx.Response(200, json={"id": "m1"}))
    await client.give(42, "i1", "w1", "e1", Decimal("12"))

    body = json.loads(requests[0].content)
    assert requests[0].url.path == "/api/bot/give"
    assert body == {"itemId": "i1", "warehouseId": "w1", "employeeId": "e1", "qty": 12}
    assert isinstance(body["qty"], int)


async def test_on_hand_counts_good_stock_only():
    client, requests = client_with(lambda r: httpx.Response(200, json=[
        {"quality": "GOOD", "qty": 7.5}, {"quality": "DEFECTIVE", "qty": 2}]))

    assert await client.on_hand("i1", "w1") == Decimal("7.5")
    assert requests[0].url.params["locationId"] == "w1"


@pytest.mark.parametrize("response, message", [
    (httpx.Response(409, text="[Not enough Screw (GOOD) at W1: 2 on hand, 5 requested]"),
     "Not enough Screw (GOOD) at W1: 2 on hand, 5 requested"),
    (httpx.Response(400, text="[Quantity must be greater than 0, Item id is required]"),
     "Quantity must be greater than 0, Item id is required"),
    (httpx.Response(401, text="[Invalid or missing bot token]"), "Invalid or missing bot token"),
    (httpx.Response(404, json={"status": 404, "error": "Not Found", "path": "/api/x"}), "Not Found"),
])
async def test_errors_carry_backend_message(response, message):
    client, _ = client_with(lambda r: response)
    with pytest.raises(ApiError) as caught:
        await client.warehouses()
    assert caught.value.status == response.status_code
    assert caught.value.message == message


async def test_unreachable_backend_is_an_api_error():
    def handler(request):
        raise httpx.ConnectError("refused", request=request)

    client, _ = client_with(handler)
    with pytest.raises(ApiError) as caught:
        await client.employees()
    assert caught.value.status == 0
    assert "not reachable" in caught.value.message
