"""Commands outside the stock dialog: /start (with optional link code), /help, and /cancel when nothing runs."""

from __future__ import annotations

from telegram import Update
from telegram.ext import ContextTypes

from ..access import FORBIDDEN, api, not_linked_text, restricted
from ..api import ApiError
from ..formatting import HELP_TEXT


async def start(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    """/start CODE links this Telegram account; plain /start greets a linked user or explains how to get linked."""
    message = update.effective_message
    user_id = update.effective_user.id
    try:
        if context.args:
            employee = await api(context).link(user_id, context.args[0])
            await message.reply_text(f"Linked: you act as {employee['name']} from now on.\n\n" + HELP_TEXT)
        else:
            employee = await api(context).me(user_id)
            await message.reply_text(f"Coperti warehouse bot. You act as {employee['name']}.\n\n" + HELP_TEXT)
    except ApiError as exc:
        if exc.status == FORBIDDEN:
            await message.reply_text(not_linked_text(user_id))
        elif context.args and exc.status == 400:
            await message.reply_text(f"Could not link: {exc.message}. Ask an administrator for a new code.")
        else:
            await message.reply_text(f"Coperti is unavailable: {exc.message}")


@restricted
async def help_command(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    await update.effective_message.reply_text(HELP_TEXT)


async def nothing_to_cancel(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    await update.effective_message.reply_text("Nothing to cancel.")
