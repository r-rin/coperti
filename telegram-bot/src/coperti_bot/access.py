"""Who may use the bot is decided by the backend: a Telegram account must be linked to an employee there."""

from __future__ import annotations

import functools
from typing import Awaitable, Callable

from telegram import Update
from telegram.ext import ContextTypes, ConversationHandler

from .api import ApiError, CopertiClient

SETTINGS_KEY = "settings"
API_KEY = "api"

FORBIDDEN = 403


def api(context: ContextTypes.DEFAULT_TYPE) -> CopertiClient:
    return context.application.bot_data[API_KEY]


def not_linked_text(telegram_user_id: int | None) -> str:
    return ("Your Telegram account is not linked to an employee yet.\n"
            "Ask an administrator for a link code and send /start CODE here, "
            f"or give them your Telegram id: {telegram_user_id}")


Handler = Callable[[Update, ContextTypes.DEFAULT_TYPE], Awaitable[object]]


def restricted(handler: Handler) -> Handler:
    """Runs the handler only for Telegram users the backend knows as an employee."""

    @functools.wraps(handler)
    async def wrapper(update: Update, context: ContextTypes.DEFAULT_TYPE):
        user = update.effective_user
        try:
            await api(context).me(user.id)
        except ApiError as exc:
            text = not_linked_text(user.id) if exc.status == FORBIDDEN else f"Coperti is unavailable: {exc.message}"
            await _tell(update, text)
            return ConversationHandler.END
        return await handler(update, context)

    return wrapper


async def _tell(update: Update, text: str) -> None:
    if update.callback_query:
        await update.callback_query.answer(text, show_alert=True)
    elif update.effective_message:
        await update.effective_message.reply_text(text)
