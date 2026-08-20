package com.github.rrin.expense.repository;

import com.github.rrin.expense.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<UUID, Expense> {
}
