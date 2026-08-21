package com.github.rrin.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStepResponse {
    private UUID id;
    private UUID processId;
    private int seq;
    private UUID operationId;
    private UUID outputItemId;
    private int outputQuantity;
    private List<ProcessComponentResponse> components;
}
