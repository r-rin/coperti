package com.github.rrin.expense.repository;

import com.github.rrin.expense.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID>, JpaSpecificationExecutor<Budget>, BudgetRepositoryCustom {
}
