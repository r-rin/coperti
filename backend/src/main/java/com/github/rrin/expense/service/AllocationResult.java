package com.github.rrin.expense.service;

import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.Funding;

import java.math.BigDecimal;
import java.util.List;

/**
 * Outcome of one allocation. Allocation is a transaction script rather than a single aggregate,
 * so it reports the rows it wrote plus the disbursement state they left behind.
 */
public record AllocationResult(
        List<Funding> fundingsCreated,
        BigDecimal remainingBalance,
        DisbursementStatus disbursementStatus
) {
}
