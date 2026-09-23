package com.github.rrin.expense.service;

import com.github.rrin.expense.dto.SettlementRequest;

/**
 * Expense-centric — "pay this worker for these receipts". The uncovered part of an expense is
 * already what the facility owes; settling it spends the worker's unspent advances first, then
 * hands over new cash. Expenses are covered oldest first, and new cash beyond the selection's
 * debt stays with the worker as an OPEN advance.
 */
public interface SettlementService {
    SettlementResult settle(SettlementRequest request);
}
