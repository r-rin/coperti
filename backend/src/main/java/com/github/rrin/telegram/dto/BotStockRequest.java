package com.github.rrin.telegram.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/** Body of the bot's receive and give actions; employeeId (the recipient) is used by give only. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotStockRequest {
    private UUID itemId;
    private UUID warehouseId;
    private UUID employeeId;
    private BigDecimal qty;
}
