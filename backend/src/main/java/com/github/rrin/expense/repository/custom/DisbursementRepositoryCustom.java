package com.github.rrin.expense.repository.custom;

import com.github.rrin.expense.Disbursement;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public interface DisbursementRepositoryCustom {
    BigDecimal sumAmount(Specification<Disbursement> spec);
}
