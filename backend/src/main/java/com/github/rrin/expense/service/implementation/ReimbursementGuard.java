package com.github.rrin.expense.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.expense.Expense;
import com.github.rrin.expense.Reimbursement;
import com.github.rrin.expense.ReimbursementStatus;

import java.math.BigDecimal;

final class ReimbursementGuard {

    private ReimbursementGuard() {
    }

    // one expense owes a worker at most once; the unique FK says so too, but a 409 beats a constraint violation
    static void requireNotAlreadyRaised(Expense expense, boolean alreadyRaised) {
        new ValidationCheck()
                .check(!alreadyRaised,
                        "Expense " + expense.getId() + " already has a reimbursement")
                .throwIfAny(ConflictException::new);
    }

    static void requireShortfall(Expense expense, BigDecimal shortfall) {
        new ValidationCheck()
                .check(shortfall.compareTo(BigDecimal.ZERO) > 0,
                        "Expense " + expense.getId() + " is fully covered, there is nothing to reimburse")
                .throwIfAny(ConflictException::new);
    }

    // only an outstanding debt can be paid; PAID is terminal and REJECTED was a decision not to pay
    static void requirePayable(Reimbursement reimbursement) {
        new ValidationCheck()
                .check(reimbursement.getStatus() == ReimbursementStatus.PENDING,
                        "Cannot pay reimbursement " + reimbursement.getId() + ": it is "
                                + reimbursement.getStatus() + ", only PENDING reimbursements can be paid")
                .throwIfAny(ConflictException::new);
    }
}
