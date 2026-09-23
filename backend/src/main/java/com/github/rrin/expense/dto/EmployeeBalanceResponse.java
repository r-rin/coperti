package com.github.rrin.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeBalanceResponse {
    private UUID employeeId;
    private String employeeName;
    private BigDecimal spent;
    private BigDecimal covered;
    private BigDecimal owed;
    private BigDecimal advanced;
    private BigDecimal unspent;
}
