package com.github.rrin.production;

import com.github.rrin.identity.Employee;
import com.github.rrin.item.Item;
import com.github.rrin.process.Operation;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    // subtask hierarchy: a subtask usually fabricates a semi-product that feeds its parent
    @ManyToOne
    @JoinColumn(name = "parent_step_id")
    private WorkOrderStep parent;

    private int seq;

    @ManyToOne
    private Operation operation;

    @ManyToOne
    private Item outputItem;

    @Column(precision = 12, scale = 3)
    private BigDecimal outputQuantity;

    // live value only; StepAssignment keeps the history
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private Employee assignee;

    private Instant plannedStart;

    // null means open-ended
    private Instant deadline;

    // live value only; StepStatusHistory keeps the history
    @Enumerated(EnumType.STRING)
    private WorkOrderStepStatus status;

    private Instant startedAt;

    private Instant completedAt;
}
