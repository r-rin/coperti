package com.github.rrin.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProcessComponentRequest {
    private UUID id;
    private UUID consumedItemId;
    private UUID consumableCategoryId;
    private int consumedQuantity;
}
