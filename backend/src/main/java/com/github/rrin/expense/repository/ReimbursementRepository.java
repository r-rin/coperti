package com.github.rrin.expense.repository;

import com.github.rrin.expense.Reimbursement;
import com.github.rrin.expense.repository.custom.ReimbursementRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ReimbursementRepository extends JpaRepository<Reimbursement, UUID>, JpaSpecificationExecutor<Reimbursement>, ReimbursementRepositoryCustom {
    boolean existsByExpenseId(UUID expenseId);
}
