package com.github.rrin.expense.repository.projection;

import com.github.rrin.expense.DisbursementStatus;

import java.math.BigDecimal;
import java.util.UUID;

/** One advance and the total drawn from it by funding rows. */
public record AdvanceDrawRow(UUID employeeId, DisbursementStatus status, BigDecimal amount, BigDecimal drawn) {
}
