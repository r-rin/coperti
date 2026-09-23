package com.github.rrin.expense.repository.custom.impl;

import com.github.rrin.expense.Reimbursement;
import com.github.rrin.expense.repository.custom.ReimbursementRepositoryCustom;
import com.github.rrin.utils.SpecificationSumSupport;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ReibursementRepositoryCustomImpl extends SpecificationSumSupport<Reimbursement> implements ReimbursementRepositoryCustom {
    public ReibursementRepositoryCustomImpl(EntityManager entityManager) {
        super(entityManager, Reimbursement.class);
    }


    @Override
    public BigDecimal sumAmount(Specification<Reimbursement> spec) {
        return sum(spec, "amount");
    }
}
