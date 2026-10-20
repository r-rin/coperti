package com.github.rrin.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepAssignmentResponse {
    private UUID id;
    private UUID stepId;
    private UUID employeeId;
    private Instant assignedAt;
    private Instant unassignedAt;
    private UUID assignedById;
}
