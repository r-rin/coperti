from decimal import Decimal

import pytest

from coperti_bot.formatting import fmt_qty, give_done, location_label, parse_quantity, receive_summary


@pytest.mark.parametrize("text, expected", [
    ("12", Decimal("12")),
    (" 2,5 ", Decimal("2.5")),
    ("0.125", Decimal("0.125")),
    ("1 000", Decimal("1000")),
    ("10.500", Decimal("10.5")),
])
def test_parse_quantity_accepts(text, expected):
    assert parse_quantity(text) == expected


@pytest.mark.parametrize("text", ["", "abc", "0", "-3", "1.2345", "NaN", "Infinity", "1e400"])
def test_parse_quantity_rejects(text):
    assert parse_quantity(text) is None


@pytest.mark.parametrize("value, expected", [
    (Decimal("12.000"), "12"), (Decimal("2.500"), "2.5"), (30.0, "30"), ("1E+2", "100"), (Decimal("0.000"), "0"),
])
def test_fmt_qty(value, expected):
    assert fmt_qty(value) == expected


def test_location_label():
    assert location_label({"code": "W1", "name": "Main"}) == "W1 · Main"
    assert location_label({"code": "W1", "name": None}) == "W1"
    assert location_label({"code": "W1", "name": "W1"}) == "W1"


def test_texts():
    assert receive_summary("Screw", Decimal("12.0"), "W1") == "Receive 12 × Screw into W1?"
    assert give_done("Screw", Decimal("3"), "W1", "Ann", Decimal("7.000")) == \
        "Recorded: gave 3 × Screw from W1 to Ann. Left at W1: 7."
    assert give_done("Screw", Decimal("3"), "W1", "Ann", None) == "Recorded: gave 3 × Screw from W1 to Ann."
