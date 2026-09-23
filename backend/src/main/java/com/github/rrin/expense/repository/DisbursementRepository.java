package com.github.rrin.expense.repository;

import com.github.rrin.expense.Disbursement;
import com.github.rrin.expense.DisbursementStatus;
import com.github.rrin.expense.repository.custom.DisbursementRepositoryCustom;
import com.github.rrin.expense.repository.projection.AdvanceDrawRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DisbursementRepository extends JpaRepository<Disbursement, UUID>, JpaSpecificationExecutor<Disbursement>, DisbursementRepositoryCustom {

    // oldest first, so settlement spends the cash a worker has been holding longest
    List<Disbursement> findByEmployeeIdAndStatusOrderByDateAscIdAsc(UUID employeeId, DisbursementStatus status);

    // one row per advance with what has been drawn from it; balances are derived, never stored
    @Query("SELECT new com.github.rrin.expense.repository.projection.AdvanceDrawRow("
            + "d.employee.id, d.status, d.amount, COALESCE(SUM(f.amountCovered), 0)) "
            + "FROM Disbursement d LEFT JOIN Funding f ON f.disbursement = d "
            + "GROUP BY d.id, d.employee.id, d.status, d.amount")
    List<AdvanceDrawRow> findAdvanceDrawRows();
}
