"""Thin async client for the Coperti REST API; every stock rule stays in the backend.

Every request carries the bot's secret token. Actions taken for a person (who am I, link, receive, give) go to
/api/bot and carry the Telegram user id; the backend decides which employee that is.
"""

from __future__ import annotations

import json
from decimal import Decimal
from typing import Any

import httpx

GOOD = "GOOD"
TOKEN_HEADER = "X-Bot-Token"
USER_HEADER = "X-Telegram-User-Id"


class ApiError(Exception):
    """A non-2xx response, carrying the backend's message in readable form."""

    def __init__(self, status: int, message: str):
        super().__init__(f"{status}: {message}")
        self.status = status
        self.message = message


def error_message(response: httpx.Response) -> str:
    """The backend answers errors either as plain "[msg, msg]" text or as Spring's JSON error object."""
    text = response.text.strip()
    try:
        body = json.loads(text)
    except ValueError:
        body = None
    if isinstance(body, dict):
        return str(body.get("message") or body.get("error") or text)
    if isinstance(body, list):
        return "; ".join(str(part) for part in body)
    if text.startswith("[") and text.endswith("]"):
        text = text[1:-1].strip()
    return text or response.reason_phrase or f"HTTP {response.status_code}"


def _number(qty: Decimal) -> int | float:
    # JSON has no decimal type; for quantities with at most three decimals the float repr is exact text
    return int(qty) if qty == qty.to_integral_value() else float(qty)


class CopertiClient:
    def __init__(self, base_url: str, bot_token: str, transport: httpx.AsyncBaseTransport | None = None):
        self._http = httpx.AsyncClient(base_url=base_url.rstrip("/") + "/", timeout=10.0, transport=transport,
                                       headers={TOKEN_HEADER: bot_token})

    async def close(self) -> None:
        await self._http.aclose()

    async def _request(self, method: str, path: str, telegram_user_id: int | None = None, **kwargs: Any) -> Any:
        headers = {USER_HEADER: str(telegram_user_id)} if telegram_user_id is not None else None
        try:
            response = await self._http.request(method, path, headers=headers, **kwargs)
        except httpx.HTTPError as exc:
            raise ApiError(0, f"Coperti is not reachable ({exc.__class__.__name__})") from exc
        if response.is_error:
            raise ApiError(response.status_code, error_message(response))
        return response.json() if response.content else None

    # ---- acting as a Telegram user (/api/bot, the backend resolves the employee) ----

    async def me(self, telegram_user_id: int) -> dict:
        """The employee this Telegram user is linked to; ApiError 403 when not linked."""
        return await self._request("GET", "bot/me", telegram_user_id)

    async def link(self, telegram_user_id: int, code: str) -> dict:
        """Redeems a link code an administrator issued; returns the employee now linked."""
        return await self._request("POST", "bot/link", telegram_user_id, json={"code": code})

    async def receive(self, telegram_user_id: int, item_id: str, warehouse_id: str, qty: Decimal) -> dict:
        return await self._request("POST", "bot/receive", telegram_user_id, json={
            "itemId": item_id, "warehouseId": warehouse_id, "qty": _number(qty)})

    async def give(self, telegram_user_id: int, item_id: str, warehouse_id: str, employee_id: str,
                   qty: Decimal) -> dict:
        return await self._request("POST", "bot/give", telegram_user_id, json={
            "itemId": item_id, "warehouseId": warehouse_id, "employeeId": employee_id, "qty": _number(qty)})

    # ---- reads (public API, still open until the whole API gets authentication) ----

    async def search_items(self, text: str) -> list[dict]:
        return await self._request("GET", "item", params={"search": text})

    async def warehouses(self) -> list[dict]:
        return await self._request("GET", "location", params={"kind": "WAREHOUSE"})

    async def employees(self) -> list[dict]:
        return await self._request("GET", "employee")

    async def balances(self, item_id: str, location_id: str | None = None) -> list[dict]:
        params = {"itemId": item_id}
        if location_id:
            params["locationId"] = location_id
        return await self._request("GET", "stock-balance", params=params)

    async def on_hand(self, item_id: str, location_id: str) -> Decimal:
        """GOOD quantity of the item at one location."""
        balances = await self.balances(item_id, location_id)
        return sum((Decimal(str(b["qty"])) for b in balances if b["quality"] == GOOD), Decimal(0))
