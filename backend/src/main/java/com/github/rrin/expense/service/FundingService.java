package com.github.rrin.expense.service;

import com.github.rrin.expense.Funding;
import com.github.rrin.expense.dto.FundingRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Mechanics, not decisions: creates a valid link between a disbursement and an expense and answers
 * coverage questions. It does not choose which disbursement pays for which expense — that is
 * {@link AllocationService}'s job. Every figure here is derived by summing Funding rows.
 */
public interface FundingService {
    Funding create(FundingRequest request);

    List<Funding> getByExpense(UUID expenseId);
    List<Funding> getByDisbursement(UUID disbursementId);

    /** How much of the expense is already paid for. */
    BigDecimal getCoveredAmount(UUID expenseId);

    /** Expense amount minus coverage, floored at zero — what the facility still owes the worker. */
    BigDecimal getShortfall(UUID expenseId);

    /** Disbursement amount minus everything drawn from it — what the worker still has to account for. */
    BigDecimal getRemainingBalance(UUID disbursementId);
}
