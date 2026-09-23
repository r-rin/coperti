package com.github.rrin.expense.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

/** One expense and the total covered on it by funding rows. */
public record ExpenseCoverageRow(UUID employeeId, BigDecimal amount, BigDecimal covered) {
}
