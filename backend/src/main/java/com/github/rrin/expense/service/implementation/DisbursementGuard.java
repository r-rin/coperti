package com.github.rrin.expense.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;

final class DisbursementGuard {

    private DisbursementGuard() {
    }

    // OPEN is the only live state: an advance settles (CLOSED) or is voided (CANCELLED), and never comes back
    static void requireTransition(Disbursement disbursement, DisbursementStatus target) {
        new ValidationCheck()
                .check(disbursement.getStatus() == DisbursementStatus.OPEN,
                        "Cannot change status of disbursement " + disbursement.getId() + ": it is "
                                + disbursement.getStatus() + ", only OPEN disbursements can be settled or cancelled")
                .check(target != DisbursementStatus.OPEN,
                        "Cannot reopen disbursement " + disbursement.getId()
                                + ": OPEN is the initial state only")
                .throwIfAny(ConflictException::new);
    }

    // CANCELLED asserts the money never left the facility, so nothing may have been spent against it
    static void requireNothingFunded(Disbursement disbursement, boolean hasFunding) {
        new ValidationCheck()
                .check(!hasFunding,
                        "Cannot cancel disbursement " + disbursement.getId()
                                + ": expenses have already been funded from it")
                .throwIfAny(ConflictException::new);
    }
}
