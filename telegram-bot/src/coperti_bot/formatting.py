"""Parsing user input and rendering the texts the bot sends."""

from __future__ import annotations

from decimal import Decimal, InvalidOperation

# the backend stores quantities as numeric(12, 3)
MAX_DECIMALS = 3
MAX_QUANTITY = Decimal("1e9")


def parse_quantity(text: str) -> Decimal | None:
    """A positive number with at most three decimals; accepts a decimal comma. None when unusable."""
    cleaned = text.strip().replace(" ", "").replace(",", ".")
    try:
        qty = Decimal(cleaned)
    except InvalidOperation:
        return None
    if not qty.is_finite() or not 0 < qty < MAX_QUANTITY or -qty.normalize().as_tuple().exponent > MAX_DECIMALS:
        return None
    return qty


def fmt_qty(qty: Decimal | int | float | str) -> str:
    """12.000 -> "12", 2.500 -> "2.5", never scientific notation."""
    value = Decimal(str(qty)).normalize()
    text = format(value, "f")
    return "0" if text in ("-0", "") else text


def location_label(location: dict) -> str:
    name = location.get("name")
    return f"{location['code']} · {name}" if name and name != location["code"] else location["code"]


def receive_summary(item: str, qty: Decimal, warehouse: str) -> str:
    return f"Receive {fmt_qty(qty)} × {item} into {warehouse}?"


def give_summary(item: str, qty: Decimal, warehouse: str, employee: str) -> str:
    return f"Give {fmt_qty(qty)} × {item} from {warehouse} to {employee}?"


def receive_done(item: str, qty: Decimal, warehouse: str, on_hand: Decimal | None) -> str:
    text = f"Recorded: received {fmt_qty(qty)} × {item} into {warehouse}."
    return text if on_hand is None else f"{text} On hand there now: {fmt_qty(on_hand)}."


def give_done(item: str, qty: Decimal, warehouse: str, employee: str, on_hand: Decimal | None) -> str:
    text = f"Recorded: gave {fmt_qty(qty)} × {item} from {warehouse} to {employee}."
    return text if on_hand is None else f"{text} Left at {warehouse}: {fmt_qty(on_hand)}."


HELP_TEXT = (
    "/receive — record supplies arriving at a warehouse\n"
    "/give — hand stock from a warehouse to an employee\n"
    "/cancel — stop the current operation"
)
