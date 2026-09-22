package com.github.rrin.expense.repository.custom.impl;

import com.github.rrin.expense.Expense;
import com.github.rrin.expense.repository.custom.ExpenseRepositoryCustom;
import com.github.rrin.utils.SpecificationSumSupport;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ExpenseRepositoryCustomImpl extends SpecificationSumSupport<Expense> implements ExpenseRepositoryCustom {

    public ExpenseRepositoryCustomImpl(EntityManager entityManager) {
        super(entityManager, Expense.class);
    }

    @Override
    public BigDecimal sumAmount(Specification<Expense> spec) {
        return sum(spec, "amount");
    }
}
