"""Drives the /receive and /give dialogs with stand-ins for Telegram objects and the API client."""

from decimal import Decimal

from telegram.ext import ConversationHandler

from coperti_bot import keyboards
from coperti_bot.api import ApiError
from coperti_bot.handlers import stock
from fakes import STRANGER, USER, FakeApi, button_update, buttons, make_context, text_update


async def test_receive_happy_path():
    api = FakeApi()
    context = make_context(api)

    assert await stock.start_receive(text_update("/receive"), context) == stock.ITEM_QUERY

    search = text_update("screw")
    assert await stock.item_query(search, context) == stock.ITEM_PICK
    assert buttons(search.message.replies[-1][1]) == ["item:screw"]

    # a single warehouse is picked automatically
    pick = button_update("item:screw")
    assert await stock.item_picked(pick, context) == stock.QUANTITY
    assert pick.callback_query.message.replies[0][0] == "Warehouse: W1 · Main"

    qty = text_update("12,5")
    assert await stock.quantity_entered(qty, context) == stock.CONFIRM
    assert qty.message.replies[-1][0] == "Receive 12.5 × Screw M4 into W1 · Main?"

    confirm = button_update(keyboards.CONFIRM)
    assert await stock.confirmed(confirm, context) == ConversationHandler.END
    assert api.posted == [("receive", USER, "screw", "w1", Decimal("12.5"))]
    assert confirm.callback_query.edits[-1] == "Recorded: received 12.5 × Screw M4 into W1 · Main. On hand there now: 30."
    assert context.user_data == {}


async def test_receive_offers_choice_of_several_warehouses():
    api = FakeApi()
    api.warehouse_list.append({"id": "w2", "code": "W2", "name": "Annex"})
    context = make_context(api)
    await stock.start_receive(text_update("/receive"), context)
    await stock.item_query(text_update("screw"), context)

    pick = button_update("item:screw")
    assert await stock.item_picked(pick, context) == stock.WAREHOUSE
    assert buttons(pick.callback_query.message.replies[-1][1]) == ["wh:w1", "wh:w2"]

    assert await stock.warehouse_picked(button_update("wh:w2"), context) == stock.QUANTITY
    assert context.user_data["warehouse"] == ("w2", "W2 · Annex")


async def test_nothing_found_keeps_asking():
    context = make_context(FakeApi())
    await stock.start_receive(text_update("/receive"), context)
    update = text_update("bolt")
    assert await stock.item_query(update, context) == stock.ITEM_QUERY
    assert "Nothing matches" in update.message.replies[-1][0]


async def test_give_happy_path():
    api = FakeApi()
    api.warehouse_list.append({"id": "w2", "code": "W2", "name": "Empty"})
    api.balance_list += [{"locationId": "w1", "quality": "DEFECTIVE", "qty": 5},
                         {"locationId": "hands", "quality": "GOOD", "qty": 3}]
    context = make_context(api)
    await stock.start_give(text_update("/give"), context)
    await stock.item_query(text_update("screw"), context)

    pick = button_update("item:screw")
    assert await stock.item_picked(pick, context) == stock.WAREHOUSE
    # only warehouses holding good stock are offered; employee locations and defective stock don't count
    text, markup = pick.callback_query.message.replies[-1]
    assert buttons(markup) == ["wh:w1"]
    assert markup.inline_keyboard[0][0].text == "W1 · Main — 20 on hand"

    warehouse = button_update("wh:w1")
    assert await stock.warehouse_picked(warehouse, context) == stock.EMPLOYEE
    assert buttons(warehouse.callback_query.message.replies[-1][1]) == ["emp:e1"]

    employee = button_update("emp:e1")
    assert await stock.employee_picked(employee, context) == stock.QUANTITY
    assert employee.callback_query.message.replies[-1][0] == "How many (up to 20)?"

    too_many = text_update("25")
    assert await stock.quantity_entered(too_many, context) == stock.QUANTITY
    assert "Only 20 on hand" in too_many.message.replies[-1][0]

    qty = text_update("4")
    assert await stock.quantity_entered(qty, context) == stock.CONFIRM
    assert qty.message.replies[-1][0] == "Give 4 × Screw M4 from W1 · Main to Ann?"

    confirm = button_update(keyboards.CONFIRM)
    assert await stock.confirmed(confirm, context) == ConversationHandler.END
    assert api.posted == [("give", USER, "screw", "w1", "e1", Decimal("4"))]
    assert confirm.callback_query.edits[-1] == "Recorded: gave 4 × Screw M4 from W1 · Main to Ann. Left at W1 · Main: 30."


async def test_give_without_stock_ends():
    api = FakeApi()
    api.balance_list = []
    context = make_context(api)
    await stock.start_give(text_update("/give"), context)
    await stock.item_query(text_update("screw"), context)

    pick = button_update("item:screw")
    assert await stock.item_picked(pick, context) == ConversationHandler.END
    assert pick.callback_query.message.replies[-1][0] == "There is no Screw M4 in any warehouse."


async def test_bad_quantity_is_asked_again():
    context = make_context(FakeApi())
    await stock.start_receive(text_update("/receive"), context)
    await stock.item_query(text_update("screw"), context)
    await stock.item_picked(button_update("item:screw"), context)

    update = text_update("-3")
    assert await stock.quantity_entered(update, context) == stock.QUANTITY
    assert "positive number" in update.message.replies[-1][0]


async def test_backend_rejection_is_shown_and_nothing_kept():
    api = FakeApi()
    api.fail_with = ApiError(409, "Not enough Screw M4 (GOOD) at W1: 2 on hand, 4 requested")
    context = make_context(api)
    await stock.start_receive(text_update("/receive"), context)
    await stock.item_query(text_update("screw"), context)
    await stock.item_picked(button_update("item:screw"), context)
    await stock.quantity_entered(text_update("4"), context)

    confirm = button_update(keyboards.CONFIRM)
    assert await stock.confirmed(confirm, context) == ConversationHandler.END
    assert confirm.callback_query.edits[-1].startswith("Not recorded: Not enough Screw M4")
    assert context.user_data == {}


async def test_cancel_button_records_nothing():
    api = FakeApi()
    context = make_context(api)
    await stock.start_receive(text_update("/receive"), context)
    await stock.item_query(text_update("screw"), context)
    await stock.item_picked(button_update("item:screw"), context)
    await stock.quantity_entered(text_update("4"), context)

    cancel = button_update(keyboards.CANCEL)
    assert await stock.confirmed(cancel, context) == ConversationHandler.END
    assert api.posted == []


async def test_stale_button_restarts_search():
    context = make_context(FakeApi())
    await stock.start_receive(text_update("/receive"), context)
    update = button_update("item:unknown")
    assert await stock.item_picked(update, context) == stock.ITEM_QUERY
    assert update.callback_query.edits[-1] == stock.STALE


async def test_unlinked_user_is_told_how_to_link():
    context = make_context(FakeApi())
    update = text_update("/receive", user=STRANGER)
    assert await stock.start_receive(update, context) == ConversationHandler.END
    reply = update.message.replies[-1][0]
    assert "not linked" in reply and "/start CODE" in reply and str(STRANGER) in reply
    assert context.user_data == {}


async def test_backend_down_is_not_reported_as_unlinked():
    api = FakeApi()
    api.unavailable = True
    update = text_update("/give")
    assert await stock.start_give(update, make_context(api)) == ConversationHandler.END
    assert update.message.replies[-1][0].startswith("Coperti is unavailable")


async def test_write_refused_when_link_removed_mid_dialog():
    api = FakeApi()
    context = make_context(api)
    await stock.start_receive(text_update("/receive"), context)
    await stock.item_query(text_update("screw"), context)
    await stock.item_picked(button_update("item:screw"), context)
    await stock.quantity_entered(text_update("1"), context)

    # the backend resolves the caller on the write itself, so an unlinked account is refused there
    api.fail_with = ApiError(403, f"Telegram account {USER} is not linked to an employee")
    confirm = button_update(keyboards.CONFIRM)
    assert await stock.confirmed(confirm, context) == ConversationHandler.END
    assert confirm.callback_query.edits[-1].startswith("Not recorded: Telegram account")
