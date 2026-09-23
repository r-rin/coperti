package com.github.rrin.expense.service;

import com.github.rrin.expense.dto.AllocationRequest;

/**
 * The decision layer: takes a split someone has already chosen and commits it through
 * {@link FundingService}, which enforces the integrity rules regardless of who decided.
 * Disbursement-centric — "this advance paid for these receipts".
 * <p>
 * A shortfall left on an expense is not turned into a Reimbursement here; an expense may still be
 * topped up from another disbursement. Raising what the facility owes is a deliberate, separate act.
 */
public interface AllocationService {
    AllocationResult allocateManual(AllocationRequest request);
}
