package com.github.rrin.production.dto;

import com.github.rrin.production.ProductionOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderResponse {
    private UUID id;
    private String code;
    private UUID productId;
    private BigDecimal quantity;
    private LocalDate dueDate;
    private String description;
    private UUID responsibleId;
    private UUID createdById;
    private ProductionOrderStatus status;
    private Instant createdAt;
}
