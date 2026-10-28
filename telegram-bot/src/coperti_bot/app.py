"""Wires settings, the API client and the handlers into a python-telegram-bot Application."""

from __future__ import annotations

from telegram import BotCommand
from telegram.ext import Application, CommandHandler

from .access import API_KEY, SETTINGS_KEY
from .api import CopertiClient
from .config import Settings
from .handlers import common, stock

COMMANDS = [
    BotCommand("receive", "Record supplies arriving at a warehouse"),
    BotCommand("give", "Hand stock from a warehouse to an employee"),
    BotCommand("cancel", "Stop the current operation"),
    BotCommand("help", "Show available commands"),
]


async def _post_init(application: Application) -> None:
    await application.bot.set_my_commands(COMMANDS)


async def _post_shutdown(application: Application) -> None:
    await application.bot_data[API_KEY].close()


def build_application(settings: Settings) -> Application:
    application = (Application.builder()
                   .token(settings.token)
                   .post_init(_post_init)
                   .post_shutdown(_post_shutdown)
                   .build())
    application.bot_data[SETTINGS_KEY] = settings
    application.bot_data[API_KEY] = CopertiClient(settings.api_url, settings.bot_token)

    application.add_handler(stock.build_conversation())
    application.add_handler(CommandHandler("start", common.start))
    application.add_handler(CommandHandler("help", common.help_command))
    # only reached when no dialog is running; inside one, the conversation's own /cancel wins
    application.add_handler(CommandHandler("cancel", common.nothing_to_cancel))
    return application
