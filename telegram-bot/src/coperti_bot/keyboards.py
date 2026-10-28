"""Inline keyboards. Callback data is "<kind>:<id>", well under Telegram's 64-byte limit for UUIDs."""

from __future__ import annotations

from typing import Iterable

from telegram import InlineKeyboardButton, InlineKeyboardMarkup

ITEM = "item"
WAREHOUSE = "wh"
EMPLOYEE = "emp"
CONFIRM = "confirm"
CANCEL = "cancel"


def choices(kind: str, options: Iterable[tuple[str, str]]) -> InlineKeyboardMarkup:
    """One button per row: (id, label)."""
    return InlineKeyboardMarkup([[InlineKeyboardButton(label, callback_data=f"{kind}:{option_id}")]
                                 for option_id, label in options])


def confirm() -> InlineKeyboardMarkup:
    return InlineKeyboardMarkup([[
        InlineKeyboardButton("Confirm", callback_data=CONFIRM),
        InlineKeyboardButton("Cancel", callback_data=CANCEL),
    ]])


def pattern(kind: str) -> str:
    return rf"^{kind}:"


def chosen_id(data: str) -> str:
    return data.split(":", 1)[1]
