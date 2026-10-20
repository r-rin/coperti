package com.github.rrin.production.repository;

import com.github.rrin.production.StepStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StepStatusHistoryRepository extends JpaRepository<StepStatusHistory, UUID> {
    List<StepStatusHistory> findAllByStep_IdOrderByChangedAtAsc(UUID stepId);
}
