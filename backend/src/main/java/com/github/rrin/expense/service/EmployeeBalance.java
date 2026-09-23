package com.github.rrin.expense.service;

import com.github.rrin.identity.Employee;

import java.math.BigDecimal;

/**
 * Where the facility stands with one employee, derived from the ledger on every read.
 *
 * @param spent    every expense they recorded
 * @param covered  the part of those expenses already paid for by advances
 * @param owed     the uncovered part — what the facility owes them
 * @param advanced cash handed to them on OPEN or CLOSED advances (CANCELLED never left the till)
 * @param unspent  what is left on their OPEN advances — cash they hold and have not yet spent against
 */
public record EmployeeBalance(
        Employee employee,
        BigDecimal spent,
        BigDecimal covered,
        BigDecimal owed,
        BigDecimal advanced,
        BigDecimal unspent
) {
}
