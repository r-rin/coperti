package com.github.rrin.expense.repository;

import com.github.rrin.expense.Funding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface FundingRepository extends JpaRepository<Funding, UUID> {

    boolean existsByDisbursementId(UUID disbursementId);

    List<Funding> findByDisbursementId(UUID disbursementId);

    List<Funding> findByExpenseId(UUID expenseId);

    // no balance is stored anywhere; every coverage figure is summed from these rows
    @Query("SELECT COALESCE(SUM(f.amountCovered), 0) FROM Funding f WHERE f.disbursement.id = :disbursementId")
    BigDecimal sumAmountCoveredByDisbursementId(@Param("disbursementId") UUID disbursementId);

    @Query("SELECT COALESCE(SUM(f.amountCovered), 0) FROM Funding f WHERE f.expense.id = :expenseId")
    BigDecimal sumAmountCoveredByExpenseId(@Param("expenseId") UUID expenseId);
}
