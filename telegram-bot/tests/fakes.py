"""Stand-ins for Telegram objects and the Coperti API client, shared by the handler tests."""

from decimal import Decimal
from types import SimpleNamespace

from coperti_bot.access import API_KEY, SETTINGS_KEY
from coperti_bot.api import ApiError
from coperti_bot.config import Settings

USER = 111
STRANGER = 999


class FakeMessage:
    def __init__(self, text=""):
        self.text = text
        self.replies = []

    async def reply_text(self, text, reply_markup=None):
        self.replies.append((text, reply_markup))


class FakeQuery:
    def __init__(self, data, message):
        self.data = data
        self.message = message
        self.edits = []
        self.answers = []

    async def answer(self, *args, **kwargs):
        self.answers.append((args, kwargs))

    async def edit_message_text(self, text, **kwargs):
        self.edits.append(text)


class FakeApi:
    def __init__(self):
        # telegram user id -> employee, as the backend would resolve it
        self.linked = {USER: {"id": "e0", "name": "Operator"}}
        self.codes = {"GOODCODE": {"id": "e2", "name": "Newcomer"}}
        self.items = [{"id": "screw", "name": "Screw M4"}]
        self.warehouse_list = [{"id": "w1", "code": "W1", "name": "Main"}]
        self.balance_list = [{"locationId": "w1", "quality": "GOOD", "qty": 20}]
        self.employee_list = [{"id": "e1", "name": "Ann"}]
        self.posted = []
        self.fail_with = None
        self.unavailable = False

    async def me(self, telegram_user_id):
        if self.unavailable:
            raise ApiError(0, "Coperti is not reachable (ConnectError)")
        if telegram_user_id not in self.linked:
            raise ApiError(403, f"Telegram account {telegram_user_id} is not linked to an employee")
        return self.linked[telegram_user_id]

    async def link(self, telegram_user_id, code):
        employee = self.codes.pop(code.upper(), None)
        if employee is None:
            raise ApiError(400, "Link code is invalid or expired")
        self.linked[telegram_user_id] = employee
        return employee

    async def search_items(self, text):
        return [item for item in self.items if text.lower() in item["name"].lower()]

    async def warehouses(self):
        return self.warehouse_list

    async def balances(self, item_id, location_id=None):
        return self.balance_list

    async def employees(self):
        return self.employee_list

    async def receive(self, *args):
        if self.fail_with:
            raise self.fail_with
        self.posted.append(("receive", *args))

    async def give(self, *args):
        if self.fail_with:
            raise self.fail_with
        self.posted.append(("give", *args))

    async def on_hand(self, item_id, location_id):
        return Decimal("30")


def make_context(api, args=None):
    settings = Settings(token="t", api_url="http://x", bot_token="b")
    return SimpleNamespace(user_data={}, args=args or [],
                           application=SimpleNamespace(bot_data={SETTINGS_KEY: settings, API_KEY: api}))


def text_update(text, user=USER):
    message = FakeMessage(text)
    return SimpleNamespace(effective_user=SimpleNamespace(id=user), message=message,
                           effective_message=message, callback_query=None)


def button_update(data, user=USER):
    message = FakeMessage()
    query = FakeQuery(data, message)
    return SimpleNamespace(effective_user=SimpleNamespace(id=user), message=None,
                           effective_message=message, callback_query=query)


def buttons(markup):
    return [row[0].callback_data for row in markup.inline_keyboard]
