package com.github.rrin.inventory;

import com.github.rrin.identity.Employee;
import com.github.rrin.item.Item;
import com.github.rrin.production.WorkOrder;
import com.github.rrin.production.WorkOrderStep;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * One row of the append-only stock ledger. Rows are never updated or deleted: a correction is a compensating row
 * pointing back through {@code reversalOf}. Every column is {@code updatable = false} to make that structural.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false, updatable = false)
    private Item item;

    @Column(nullable = false, updatable = false, precision = 12, scale = 3)
    private BigDecimal qty;

    @ManyToOne
    @JoinColumn(name = "from_location_id", nullable = false, updatable = false)
    private Location fromLocation;

    @ManyToOne
    @JoinColumn(name = "to_location_id", nullable = false, updatable = false)
    private Location toLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private QualityState fromQuality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private QualityState toQuality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private MovementType movementType;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    @ManyToOne
    @JoinColumn(name = "performed_by_id", updatable = false)
    private Employee performedBy;

    @ManyToOne
    @JoinColumn(name = "work_order_id", updatable = false)
    private WorkOrder workOrder;

    @ManyToOne
    @JoinColumn(name = "step_id", updatable = false)
    private WorkOrderStep step;

    @ManyToOne
    @JoinColumn(name = "other_use_id", updatable = false)
    private OtherUse otherUse;

    // the row this one compensates; at most one reversal per row
    @OneToOne
    @JoinColumn(name = "reversal_of_id", unique = true, updatable = false)
    private StockMovement reversalOf;

    @Column(updatable = false)
    private String note;
}
