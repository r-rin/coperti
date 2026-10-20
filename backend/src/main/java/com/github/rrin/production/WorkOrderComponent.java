package com.github.rrin.production;

import com.github.rrin.item.Item;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/** A frozen BOM line of a work order, with any substitute category already resolved to a concrete item. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne
    @JoinColumn(name = "step_id")
    private WorkOrderStep step;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal qtyRequired;

    // on hand at the task
    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal qtyIssued = BigDecimal.ZERO;

    // actually used
    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal qtyConsumed = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal qtyScrapped = BigDecimal.ZERO;
}
