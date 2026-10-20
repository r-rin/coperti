package com.github.rrin.production;

import com.github.rrin.identity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "step_id", nullable = false)
    private WorkOrderStep step;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStepStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStepStatus toStatus;

    @Column(nullable = false)
    private Instant changedAt;

    @ManyToOne
    @JoinColumn(name = "changed_by_id")
    private Employee changedBy;
}
