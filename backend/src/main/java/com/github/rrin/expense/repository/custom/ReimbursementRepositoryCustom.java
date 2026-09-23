package com.github.rrin.expense.repository.custom;

import com.github.rrin.expense.Reimbursement;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public interface ReimbursementRepositoryCustom {
    BigDecimal sumAmount(Specification<Reimbursement> spec);
}
