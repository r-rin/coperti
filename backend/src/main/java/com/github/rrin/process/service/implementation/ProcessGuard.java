package com.github.rrin.process.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.process.Process;
import com.github.rrin.process.ProcessStatus;

final class ProcessGuard {

    private ProcessGuard() {
    }

    // ACTIVE and ARCHIVED processes are immutable snapshots referenced by work orders
    static void requireDraft(Process process, String action) {
        new ValidationCheck()
                .check(process.getStatus() == ProcessStatus.DRAFT,
                        "Cannot " + action + ": process " + process.getId() + " is " + process.getStatus()
                                + ", only DRAFT processes can be modified")
                .throwIfAny(InvalidQuery::new);
    }
}
