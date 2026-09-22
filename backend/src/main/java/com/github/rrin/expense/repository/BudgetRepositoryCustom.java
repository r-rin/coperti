package com.github.rrin.expense.repository;

import com.github.rrin.expense.Budget;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public interface BudgetRepositoryCustom {
    BigDecimal sumAmount(Specification<Budget> spec);
}
