package com.github.rrin.expense.repository;

import com.github.rrin.expense.Expense;
import com.github.rrin.expense.repository.custom.ExpenseRepositoryCustom;
import com.github.rrin.expense.repository.projection.ExpenseCoverageRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense>, ExpenseRepositoryCustom {

    // per expense rather than per employee: owed is clamped per expense, so an expense edited below
    // its funding must not offset another expense's debt
    @Query("SELECT new com.github.rrin.expense.repository.projection.ExpenseCoverageRow("
            + "e.employee.id, e.amount, COALESCE(SUM(f.amountCovered), 0)) "
            + "FROM Expense e LEFT JOIN Funding f ON f.expense = e "
            + "GROUP BY e.id, e.employee.id, e.amount")
    List<ExpenseCoverageRow> findCoverageRows();
}
