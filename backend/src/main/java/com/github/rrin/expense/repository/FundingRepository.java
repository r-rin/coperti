package com.github.rrin.expense.repository;

import com.github.rrin.expense.Funding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FundingRepository extends JpaRepository<Funding, UUID> {
}
