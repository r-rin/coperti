package com.github.rrin.telegram.repository;

import com.github.rrin.telegram.TelegramAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TelegramAccountRepository extends JpaRepository<TelegramAccount, UUID> {
    Optional<TelegramAccount> findByTelegramUserId(Long telegramUserId);
    Optional<TelegramAccount> findByEmployee_Id(UUID employeeId);
}
