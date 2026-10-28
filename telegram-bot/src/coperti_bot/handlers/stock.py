"""The /receive and /give dialogs.

Both share the first step (find the item) and the last two (quantity, confirm), so they live in one
ConversationHandler and branch on the flow chosen at entry. Starting either command again restarts the dialog.

    /receive: item → warehouse → quantity → confirm  → RECEIPT from supplier into the warehouse
    /give:    item → warehouse (with stock) → employee → quantity → confirm  → ISSUE_TO_EMPLOYEE
"""

from __future__ import annotations

import warnings
from decimal import Decimal

from telegram import Message, Update
from telegram.warnings import PTBUserWarning
from telegram.ext import (CallbackQueryHandler, CommandHandler, ContextTypes, ConversationHandler,
                          MessageHandler, filters)

from .. import keyboards
from ..access import api as _api, restricted
from ..api import GOOD, ApiError
from ..formatting import (fmt_qty, give_done, give_summary, location_label, parse_quantity, receive_done,
                          receive_summary)

ITEM_QUERY, ITEM_PICK, WAREHOUSE, EMPLOYEE, QUANTITY, CONFIRM = range(6)
RECEIVE, GIVE = "receive", "give"

STALE = "That list is out of date. Type the item name again."


# ---- entry ---------------------------------------------------------------------------------------------------

@restricted
async def start_receive(update: Update, context: ContextTypes.DEFAULT_TYPE) -> int:
    return await _begin(update, context, RECEIVE, "What arrived? Type part of the item name.")


@restricted
async def start_give(update: Update, context: ContextTypes.DEFAULT_TYPE) -> int:
    return await _begin(update, context, GIVE, "What are you giving out? Type part of the item name.")


async def _begin(update: Update, context: ContextTypes.DEFAULT_TYPE, flow: str, prompt: str) -> int:
    context.user_data.clear()
    context.user_data["flow"] = flow
    await update.effective_message.reply_text(prompt)
    return ITEM_QUERY


# ---- item ----------------------------------------------------------------------------------------------------

async def item_query(update: Update, context: ContextTypes.DEFAULT_TYPE) -> int:
    text = update.message.text.strip()
    try:
        items = await _api(context).search_items(text)
    except ApiError as exc:
        await update.message.reply_text(f"Search failed: {exc.message}")
        return ITEM_QUERY
    if not items:
        await update.message.reply_text(f"Nothing matches “{text}”. Try another name, or /cancel.")
        return ITEM_QUERY

    context.user_data["items"] = {item["id"]: item["name"] for item in items}
    await update.message.reply_text(
        "Pick the item (or type another name):",
        reply_markup=keyboards.choices(keyboards.ITEM, [(item["id"], item["name"]) for item in items]))
    return ITEM_PICK


async def item_picked(update: Update, context: ContextTypes.DEFAULT_TYPE) -> int:
    query = update.callback_query
    await query.answer()
    item_id = keyboards.chosen_id(query.data)
    name = context.user_data.get("items", {}).get(item_id)
    if name is None:
        await query.edit_message_text(STALE)
        return ITEM_QUERY

    context.user_data["item"] = (item_id, name)
    await query.edit_message_text(f"Item: {name}")
    if context.user_data["flow"] == RECEIVE:
        return await _ask_receive_warehouse(query.message, context)
    return await _ask_give_warehouse(query.message, context)


# ---- warehouse -----------------------------------------------------------------------------------------------

async def _ask_receive_warehouse(message: Message, context: ContextTypes.DEFAULT_TYPE) -> int:
    try:
        warehouses = await _api(context).warehouses()
    except ApiError as exc:
        return await _abort(message, context, f"Could not load warehouses: {exc.message}")
    if not warehouses:
        return await _abort(message, context, "No warehouses are set up yet.")

    labels = {w["id"]: location_label(w) for w in warehouses}
    context.user_data["warehouses"] = labels
    if len(labels) == 1:
        [(only_id, only_label)] = labels.items()
        context.user_data["warehouse"] = (only_id, only_label)
        await message.reply_text(f"Warehouse: {only_label}")
        return await _ask_quantity(message, context)

    await message.reply_text("Into which warehouse?", reply_markup=keyboards.choices(keyboards.WAREHOUSE, labels.items()))
    return WAREHOUSE


async def _ask_give_warehouse(message: Message, context: ContextTypes.DEFAULT_TYPE) -> int:
    item_id, item_name = context.user_data["item"]
    try:
        warehouses = {w["id"]: location_label(w) for w in await _api(context).warehouses()}
        balances = await _api(context).balances(item_id)
    except ApiError as exc:
        return await _abort(message, context, f"Could not load stock: {exc.message}")

    # only warehouses that actually hold good stock of the item are worth offering
    stock: dict[str, Decimal] = {}
    for balance in balances:
        if balance["quality"] == GOOD and balance["locationId"] in warehouses:
            stock[balance["locationId"]] = stock.get(balance["locationId"], Decimal(0)) + Decimal(str(balance["qty"]))
    stock = {location: qty for location, qty in stock.items() if qty > 0}
    if not stock:
        return await _abort(message, context, f"There is no {item_name} in any warehouse.")

    context.user_data["warehouses"] = {location: warehouses[location] for location in stock}
    context.user_data["stock"] = stock
    options = [(location, f"{warehouses[location]} — {fmt_qty(qty)} on hand") for location, qty in stock.items()]
    await message.reply_text("From which warehouse?", reply_markup=keyboards.choices(keyboards.WAREHOUSE, options))
    return WAREHOUSE


async def warehouse_picked(update: Update, context: ContextTypes.DEFAULT_TYPE) -> int:
    query = update.callback_query
    await query.answer()
    warehouse_id = keyboards.chosen_id(query.data)
    label = context.user_data.get("warehouses", {}).get(warehouse_id)
    if label is None:
        await query.edit_message_text(STALE)
        return ITEM_QUERY

    context.user_data["warehouse"] = (warehouse_id, label)
    await query.edit_message_text(f"Warehouse: {label}")
    if context.user_data["flow"] == RECEIVE:
        return await _ask_quantity(query.message, context)
    return await _ask_employee(query.message, context)


# ---- employee (give only) ------------------------------------------------------------------------------------

async def _ask_employee(message: Message, context: ContextTypes.DEFAULT_TYPE) -> int:
    try:
        employees = await _api(context).employees()
    except ApiError as exc:
        return await _abort(message, context, f"Could not load employees: {exc.message}")
    if not employees:
        return await _abort(message, context, "No employees are registered yet.")

    names = {e["id"]: e["name"] for e in employees}
    context.user_data["employees"] = names
    await message.reply_text("To whom?", reply_markup=keyboards.choices(keyboards.EMPLOYEE, names.items()))
    return EMPLOYEE


async def employee_picked(update: Update, context: ContextTypes.DEFAULT_TYPE) -> int:
    query = update.callback_query
    await query.answer()
    employee_id = keyboards.chosen_id(query.data)
    name = context.user_data.get("employees", {}).get(employee_id)
    if name is None:
        await query.edit_message_text(STALE)
        return ITEM_QUERY

    context.user_data["employee"] = (employee_id, name)
    await query.edit_message_text(f"To: {name}")
    return await _ask_quantity(query.message, context)


# ---- quantity and confirmation -------------------------------------------------------------------------------

def _on_hand(context: ContextTypes.DEFAULT_TYPE) -> Decimal | None:
    if context.user_data["flow"] != GIVE:
        return None
    return context.user_data["stock"][context.user_data["warehouse"][0]]


async def _ask_quantity(message: Message, context: ContextTypes.DEFAULT_TYPE) -> int:
    limit = _on_hand(context)
    hint = "" if limit is None else f" (up to {fmt_qty(limit)})"
    await message.reply_text(f"How many{hint}?")
    return QUANTITY


async def quantity_entered(update: Update, context: ContextTypes.DEFAULT_TYPE) -> int:
    qty = parse_quantity(update.message.text)
    if qty is None:
        await update.message.reply_text("Send a positive number, e.g. 12 or 2.5 (at most 3 decimals).")
        return QUANTITY
    limit = _on_hand(context)
    if limit is not None and qty > limit:
        await update.message.reply_text(f"Only {fmt_qty(limit)} on hand there. Send a smaller number.")
        return QUANTITY

    context.user_data["qty"] = qty
    item = context.user_data["item"][1]
    warehouse = context.user_data["warehouse"][1]
    summary = (receive_summary(item, qty, warehouse) if context.user_data["flow"] == RECEIVE
               else give_summary(item, qty, warehouse, context.user_data["employee"][1]))
    await update.message.reply_text(summary, reply_markup=keyboards.confirm())
    return CONFIRM


# no @restricted here: the backend resolves the caller on the write itself and refuses unlinked accounts
async def confirmed(update: Update, context: ContextTypes.DEFAULT_TYPE) -> int:
    query = update.callback_query
    await query.answer()
    if query.data == keyboards.CANCEL:
        context.user_data.clear()
        await query.edit_message_text("Cancelled. Nothing was recorded.")
        return ConversationHandler.END

    data = context.user_data
    if "qty" not in data:
        await query.edit_message_text("This confirmation has expired. Start again with /receive or /give.")
        return ConversationHandler.END
    api = _api(context)
    user_id = update.effective_user.id
    (item_id, item), (warehouse_id, warehouse), qty = data["item"], data["warehouse"], data["qty"]
    try:
        if data["flow"] == RECEIVE:
            await api.receive(user_id, item_id, warehouse_id, qty)
        else:
            employee_id, employee = data["employee"]
            await api.give(user_id, item_id, warehouse_id, employee_id, qty)
    except ApiError as exc:
        data.clear()
        await query.edit_message_text(f"Not recorded: {exc.message}")
        return ConversationHandler.END

    try:
        on_hand = await api.on_hand(item_id, warehouse_id)
    except ApiError:
        on_hand = None  # the movement is in; a failed follow-up read shouldn't look like a failure
    text = (receive_done(item, qty, warehouse, on_hand) if data["flow"] == RECEIVE
            else give_done(item, qty, warehouse, data["employee"][1], on_hand))
    data.clear()
    await query.edit_message_text(text)
    return ConversationHandler.END


async def cancel(update: Update, context: ContextTypes.DEFAULT_TYPE) -> int:
    context.user_data.clear()
    await update.effective_message.reply_text("Cancelled. Nothing was recorded.")
    return ConversationHandler.END


async def _abort(message: Message, context: ContextTypes.DEFAULT_TYPE, text: str) -> int:
    context.user_data.clear()
    await message.reply_text(text)
    return ConversationHandler.END


def build_conversation() -> ConversationHandler:
    text = filters.TEXT & ~filters.COMMAND
    # the dialog mixes typed text and button presses, so it must track per chat, not per message;
    # PTB warns about that combination on construction even though it is exactly what we want
    with warnings.catch_warnings():
        warnings.filterwarnings("ignore", message="If 'per_message=False'", category=PTBUserWarning)
        return _conversation(text)


def _conversation(text: filters.BaseFilter) -> ConversationHandler:
    return ConversationHandler(
        entry_points=[CommandHandler("receive", start_receive), CommandHandler("give", start_give)],
        states={
            ITEM_QUERY: [MessageHandler(text, item_query)],
            # typing a new name while the buttons are shown simply searches again
            ITEM_PICK: [CallbackQueryHandler(item_picked, pattern=keyboards.pattern(keyboards.ITEM)),
                        MessageHandler(text, item_query)],
            WAREHOUSE: [CallbackQueryHandler(warehouse_picked, pattern=keyboards.pattern(keyboards.WAREHOUSE))],
            EMPLOYEE: [CallbackQueryHandler(employee_picked, pattern=keyboards.pattern(keyboards.EMPLOYEE))],
            QUANTITY: [MessageHandler(text, quantity_entered)],
            CONFIRM: [CallbackQueryHandler(confirmed, pattern=rf"^({keyboards.CONFIRM}|{keyboards.CANCEL})$")],
        },
        fallbacks=[CommandHandler("cancel", cancel)],
        allow_reentry=True,
    )
