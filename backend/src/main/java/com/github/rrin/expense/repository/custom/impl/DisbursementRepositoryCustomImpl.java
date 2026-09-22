package com.github.rrin.expense.repository.custom.impl;

import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.repository.custom.DisbursementRepositoryCustom;
import com.github.rrin.utils.SpecificationSumSupport;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class DisbursementRepositoryCustomImpl extends SpecificationSumSupport<Disbursement> implements DisbursementRepositoryCustom {

    public DisbursementRepositoryCustomImpl(EntityManager entityManager) {
        super(entityManager, Disbursement.class);
    }

    @Override
    public BigDecimal sumAmount(Specification<Disbursement> spec) {
        return sum(spec, "amount");
    }
}
