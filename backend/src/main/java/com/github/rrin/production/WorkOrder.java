package com.github.rrin.production;

import com.github.rrin.identity.Employee;
import com.github.rrin.item.Item;
import com.github.rrin.process.Process;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    // null for standalone and rework jobs that have no batch behind them
    @ManyToOne
    private ProductionOrder order;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // the exact process version the work order was released from; never repointed afterwards
    @ManyToOne
    @JoinColumn(nullable = false)
    private Process process;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    private WorkOrderKind kind;

    @ManyToOne
    @JoinColumn(name = "primary_assignee_id")
    private Employee primaryAssignee;

    @Enumerated(EnumType.STRING)
    private WorkOrderStatus status;

    private Instant releasedAt;

    private Instant completedAt;
}
