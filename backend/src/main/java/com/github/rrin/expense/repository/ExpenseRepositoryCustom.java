package com.github.rrin.expense.repository;

import com.github.rrin.expense.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public interface ExpenseRepositoryCustom {
    BigDecimal sumAmount(Specification<Expense> spec);
}
