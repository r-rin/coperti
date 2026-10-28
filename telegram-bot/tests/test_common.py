"""/start: linking with a code, greeting linked users, guiding everyone else."""

from coperti_bot.handlers import common
from fakes import STRANGER, USER, FakeApi, make_context, text_update


async def test_start_with_code_links_the_account():
    api = FakeApi()
    update = text_update("/start goodcode", user=STRANGER)

    await common.start(update, make_context(api, args=["goodcode"]))

    assert update.message.replies[-1][0].startswith("Linked: you act as Newcomer")
    assert api.linked[STRANGER]["name"] == "Newcomer"


async def test_bad_code_is_explained():
    update = text_update("/start WRONG", user=STRANGER)
    await common.start(update, make_context(FakeApi(), args=["WRONG"]))
    assert update.message.replies[-1][0] == ("Could not link: Link code is invalid or expired. "
                                             "Ask an administrator for a new code.")


async def test_plain_start_greets_linked_user():
    update = text_update("/start")
    await common.start(update, make_context(FakeApi()))
    assert "You act as Operator" in update.message.replies[-1][0]


async def test_plain_start_explains_linking_to_stranger():
    update = text_update("/start", user=STRANGER)
    await common.start(update, make_context(FakeApi()))
    assert str(STRANGER) in update.message.replies[-1][0]
    assert "/start CODE" in update.message.replies[-1][0]


async def test_help_requires_link():
    update = text_update("/help", user=STRANGER)
    await common.help_command(update, make_context(FakeApi()))
    assert "not linked" in update.message.replies[-1][0]

    linked = text_update("/help", user=USER)
    await common.help_command(linked, make_context(FakeApi()))
    assert "/receive" in linked.message.replies[-1][0]
