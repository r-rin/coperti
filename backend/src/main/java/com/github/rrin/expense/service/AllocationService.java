package com.github.rrin.expense.service;

import com.github.rrin.expense.dto.AllocationRequest;

/**
 * The decision layer: takes a split someone has already chosen and commits it through
 * {@link FundingService}, which enforces the integrity rules regardless of who decided.
 * Disbursement-centric — "this advance paid for these receipts".
 * <p>
 * A shortfall left on an expense is simply what the facility still owes the employee; it may be
 * topped up from another disbursement or settled through {@link SettlementService}.
 */
public interface AllocationService {
    AllocationResult allocateManual(AllocationRequest request);
}
