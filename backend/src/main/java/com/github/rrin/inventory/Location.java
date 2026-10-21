package com.github.rrin.inventory;

import com.github.rrin.identity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/** Anywhere stock sits: a warehouse, a bench, an employee's hands, or a virtual sink that keeps movements balanced. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationKind kind;

    // set exactly when kind is EMPLOYEE: "given to worker Y" is just another place
    @OneToOne
    @JoinColumn(name = "employee_id", unique = true)
    private Employee employee;

    @PrePersist
    @PreUpdate
    public void checkEmployee() {
        if ((kind == LocationKind.EMPLOYEE) != (employee != null)) {
            throw new IllegalStateException("Employee must be set exactly for EMPLOYEE locations");
        }
    }
}
