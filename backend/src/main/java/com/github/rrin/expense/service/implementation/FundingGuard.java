package com.github.rrin.expense.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.Expense;

import java.math.BigDecimal;

final class FundingGuard {

    private FundingGuard() {
    }

    // the integrity rule that must always hold, whoever decided the split
    static void requireDrawable(Disbursement disbursement,
                                Expense expense,
                                BigDecimal amountCovered,
                                BigDecimal remaining,
                                BigDecimal shortfall) {
        new ValidationCheck()
                .check(disbursement.getStatus() == DisbursementStatus.OPEN,
                        "Cannot draw from disbursement " + disbursement.getId() + ": it is "
                                + disbursement.getStatus() + ", only OPEN disbursements have money left")
                .check(amountCovered.compareTo(remaining) <= 0,
                        "Cannot draw " + amountCovered + " from disbursement " + disbursement.getId()
                                + ": only " + remaining + " remains")
                .check(amountCovered.compareTo(shortfall) <= 0,
                        "Cannot cover " + amountCovered + " of expense " + expense.getId()
                                + ": only " + shortfall + " is still uncovered")
                .throwIfAny(ConflictException::new);
    }
}
