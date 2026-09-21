package com.github.rrin.production;

import com.github.rrin.identity.Employee;
import com.github.rrin.item.Item;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Item product;

    @Column(nullable = false)
    private int quantity;

    private LocalDate due_to;

    private String description;

    @ManyToOne
    @JoinColumn(name = "responsible_id")
    private Employee responsible;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private Employee createdBy;

    private LocalDateTime createdAt;
}
