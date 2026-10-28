package com.github.rrin.telegram;

import com.github.rrin.identity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A short-lived, single-use code an administrator hands to an employee, who sends it to the bot with /start.
 * Redeeming it proves the person controls that Telegram account, so nobody has to look up numeric ids.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramLinkCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Instant expiresAt;

    // null until redeemed; a redeemed code can't be used again
    private Instant usedAt;
}
