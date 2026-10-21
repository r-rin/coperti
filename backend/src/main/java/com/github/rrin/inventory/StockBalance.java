package com.github.rrin.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.*;

import java.math.BigDecimal;

/** Derived cache of the ledger per (item, location, quality); never a source of truth and rebuildable from it. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockBalance {

    @EmbeddedId
    private StockBalanceId id;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal qty;
}
