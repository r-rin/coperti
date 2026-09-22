package com.github.rrin.expense.service;

import com.github.rrin.expense.Budget;
import com.github.rrin.expense.dto.BudgetRequest;
import com.github.rrin.expense.dto.filter.BudgetFilter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.UUID;

public interface BudgetService {
    Budget create(BudgetRequest budgetRequest);
    Budget getById(UUID id);
    BigDecimal getFacilityFloat();

    Page<Budget> search(BudgetFilter filter, int page, int size);
    BigDecimal sum(BudgetFilter filter);
}
