package com.github.rrin.expense.service;

import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.dto.DisbursementRequest;
import com.github.rrin.expense.dto.filter.DisbursementFilter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface DisbursementService {
    Disbursement create(DisbursementRequest request);
    Disbursement updateStatus(UUID id, DisbursementStatus status);
    Disbursement getById(UUID id);

    Page<Disbursement> search(DisbursementFilter filter, int page, int size);
}
