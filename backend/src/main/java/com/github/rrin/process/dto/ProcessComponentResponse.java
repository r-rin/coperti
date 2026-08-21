package com.github.rrin.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessComponentResponse {
    private UUID id;
    private UUID processStepId;
    private UUID consumedItemId;
    private UUID consumableCategoryId;
    private int consumedQuantity;
}
