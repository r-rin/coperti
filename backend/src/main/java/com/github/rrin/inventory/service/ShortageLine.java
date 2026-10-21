package com.github.rrin.inventory.service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * What a work order still needs of one item: outstanding (required − issued), already reserved for it,
 * still free in warehouses, and what neither covers.
 */
public record ShortageLine(UUID itemId, BigDecimal outstanding, BigDecimal reserved,
                           BigDecimal freeAvailable, BigDecimal missing) {
}
