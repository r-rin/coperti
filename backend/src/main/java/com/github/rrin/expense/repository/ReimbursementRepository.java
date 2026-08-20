package com.github.rrin.expense.repository;

import com.github.rrin.expense.Reimbursement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReimbursementRepository extends JpaRepository<UUID, Reimbursement> {
}
