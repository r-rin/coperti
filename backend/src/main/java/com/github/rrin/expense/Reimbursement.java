package com.github.rrin.expense;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reimbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    private Expense expense;

    @Column(name = "amount", precision=10, scale = 2)
    @JoinColumn(name = "expense_id", nullable = false, unique = true)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @ColumnDefault("'PENDING'")
    private ReimbursementStatus status;

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "payout_disbursement_id", nullable = true, unique = true)
    private Disbursement payoutDisbursement;
}
