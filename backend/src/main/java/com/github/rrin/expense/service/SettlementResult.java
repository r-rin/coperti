package com.github.rrin.expense.service;

import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.Funding;

import java.math.BigDecimal;
import java.util.List;

/**
 * Outcome of one settlement: the funding rows written, where the money came from, and what is
 * left on either side — cash still with the worker on the new advance, debt still on the selection.
 *
 * @param newDisbursement null when the settlement used only existing advances
 */
public record SettlementResult(
        List<Funding> fundingsCreated,
        BigDecimal fromAdvances,
        BigDecimal fromNewCash,
        Disbursement newDisbursement,
        BigDecimal leftOnNewAdvance,
        BigDecimal stillOwed
) {
}
