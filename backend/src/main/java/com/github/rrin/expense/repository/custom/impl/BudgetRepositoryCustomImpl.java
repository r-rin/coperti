package com.github.rrin.expense.repository.custom.impl;

import com.github.rrin.expense.Budget;
import com.github.rrin.expense.repository.custom.BudgetRepositoryCustom;
import com.github.rrin.utils.SpecificationSumSupport;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class BudgetRepositoryCustomImpl extends SpecificationSumSupport<Budget> implements BudgetRepositoryCustom {

    public BudgetRepositoryCustomImpl(EntityManager entityManager) {
        super(entityManager, Budget.class);
    }

    @Override
    public BigDecimal sumAmount(Specification<Budget> spec) {
        return sum(spec, "amount");
    }
}
