package com.github.rrin.production;

import com.github.rrin.identity.Employee;
import com.github.rrin.item.Item;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Item product;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    private LocalDate dueDate;

    private String description;

    @ManyToOne
    @JoinColumn(name = "responsible_id")
    private Employee responsible;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private Employee createdBy;

    @Enumerated(EnumType.STRING)
    private ProductionOrderStatus status;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
