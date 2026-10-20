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
public class StepAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "step_id", nullable = false)
    private WorkOrderStep step;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Instant assignedAt;

    // null while the assignment is current
    private Instant unassignedAt;

    @ManyToOne
    @JoinColumn(name = "assigned_by_id")
    private Employee assignedBy;
}
