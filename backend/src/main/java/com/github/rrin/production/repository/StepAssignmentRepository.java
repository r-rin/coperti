package com.github.rrin.production.repository;

import com.github.rrin.production.StepAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StepAssignmentRepository extends JpaRepository<StepAssignment, UUID> {
    List<StepAssignment> findAllByStep_IdOrderByAssignedAtAsc(UUID stepId);

    Optional<StepAssignment> findFirstByStep_IdAndUnassignedAtIsNull(UUID stepId);

    // assignments that overlap [from, to]: started before the period ended and not finished before it began
    @Query("SELECT a FROM StepAssignment a WHERE a.employee.id = :employeeId "
            + "AND a.assignedAt <= :to AND (a.unassignedAt IS NULL OR a.unassignedAt >= :from) "
            + "ORDER BY a.assignedAt ASC")
    List<StepAssignment> findOverlapping(UUID employeeId, Instant from, Instant to);
}
