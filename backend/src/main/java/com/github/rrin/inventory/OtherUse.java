package com.github.rrin.inventory;

import com.github.rrin.identity.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/** A withdrawal outside the batch plan (samples, maintenance); batch calculations exclude it by construction. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtherUse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "requested_by_id", nullable = false)
    private Employee requestedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by_id")
    private Employee approvedBy;

    private String reason;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "requested_at", updatable = false)
    private Instant requestedAt;

    // when the stock actually left, i.e. on approval
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtherUseStatus status;
}
