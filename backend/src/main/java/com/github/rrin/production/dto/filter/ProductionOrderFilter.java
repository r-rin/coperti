package com.github.rrin.production.dto.filter;

import com.github.rrin.production.ProductionOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrderFilter {
    private UUID productId;
    private Set<ProductionOrderStatus> statuses;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String search;
}
