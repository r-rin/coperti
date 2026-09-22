package com.github.rrin.expense.service;

import com.github.rrin.expense.Expense;
import com.github.rrin.expense.dto.ExpenseRequest;
import com.github.rrin.expense.dto.filter.ExpenseFilter;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ExpenseService {
    Expense create(ExpenseRequest request);
    Expense update(ExpenseRequest request);
    Expense get(UUID id);
    Expense delete(UUID id);

    Page<Expense> search(ExpenseFilter filter, int page, int size);
}
