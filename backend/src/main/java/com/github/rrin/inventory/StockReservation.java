package com.github.rrin.inventory;

import com.github.rrin.item.Item;
import com.github.rrin.production.WorkOrder;
import com.github.rrin.production.WorkOrderComponent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Soft reservation (schema ALLOCATION): stock promised to a work order line so two batches can't count it twice. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne
    @JoinColumn(name = "work_order_component_id", nullable = false)
    private WorkOrderComponent workOrderComponent;

    // still-reserved quantity; issuing draws it down
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal qty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
