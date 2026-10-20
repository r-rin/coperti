package com.github.rrin.expense;

import com.github.rrin.identity.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name="amount", precision = 10, scale = 2)
    private BigDecimal amount;

    private LocalDate date;

    private String description;

    // tiebreaker for rows sharing a date: ids are random UUIDs, so they carry no insertion order
    @CreationTimestamp
    // the default backfills rows that existed before this column, so ddl-auto=update can add it NOT NULL
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
