package com.github.rrin.expense.service;

import java.util.List;

public interface BalanceService {
    /** One balance per employee, including those with nothing recorded yet. */
    List<EmployeeBalance> getEmployeeBalances();
}
