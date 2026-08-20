package com.github.rrin.expense;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Funding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne()
    @JoinColumn(name = "disbursement_id", nullable = false)
    private Disbursement disbursement;

    @ManyToOne()
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @Column(name = "amount_covered", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountCovered;
}
