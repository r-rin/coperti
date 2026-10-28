package com.github.rrin.telegram.service;

import com.github.rrin.inventory.StockMovement;
import com.github.rrin.telegram.dto.BotStockRequest;

/**
 * The bot's write actions. The acting employee is resolved here from the Telegram user id, never taken from the bot,
 * and the movements go through the regular ledger with all of its rules.
 */
public interface BotStockService {
    /** Supplies arriving: RECEIPT from the supplier into a warehouse. */
    StockMovement receive(long telegramUserId, BotStockRequest request);

    /** Stock handed out: ISSUE_TO_EMPLOYEE from a warehouse into the recipient's location. */
    StockMovement give(long telegramUserId, BotStockRequest request);
}
