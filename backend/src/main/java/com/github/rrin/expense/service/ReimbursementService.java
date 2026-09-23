package com.github.rrin.expense.service;

import com.github.rrin.expense.Reimbursement;
import com.github.rrin.expense.ReimbursementStatus;
import com.github.rrin.expense.dto.ReimbursementRequest;
import com.github.rrin.expense.dto.filter.ReimbursementFilter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.UUID;

public interface ReimbursementService {
    Reimbursement create(ReimbursementRequest reimbursementRequest);
    Reimbursement update(ReimbursementRequest reimbursementRequest);
    Reimbursement get(UUID reimbursementId);
    Reimbursement delete(UUID reimbursementId);

    Reimbursement markAsPaid(UUID reimbursementId);
    BigDecimal sum(ReimbursementFilter filter);
    Page<Reimbursement> search(ReimbursementFilter filter, int page, int size);
}
