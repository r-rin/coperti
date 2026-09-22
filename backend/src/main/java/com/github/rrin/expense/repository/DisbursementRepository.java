package com.github.rrin.expense.repository;

import com.github.rrin.expense.Disbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface DisbursementRepository extends JpaRepository<Disbursement, UUID>, JpaSpecificationExecutor<Disbursement>, DisbursementRepositoryCustom {
}
