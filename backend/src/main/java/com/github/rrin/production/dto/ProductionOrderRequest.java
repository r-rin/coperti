package com.github.rrin.production.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderRequest {
    private UUID id;
    private String code;
    private UUID productId;
    private BigDecimal quantity;
    private LocalDate dueDate;
    private String description;
    private UUID responsibleId;
    private UUID createdById;
}
