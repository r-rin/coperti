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
public class ProcessComponentRequest {
    private UUID id;
    private UUID consumedItemId;
    private UUID consumableCategoryId;
    private int consumedQuantity;
}
