package com.github.rrin.telegram.repository;

import com.github.rrin.telegram.TelegramLinkCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelegramLinkCodeRepository extends JpaRepository<TelegramLinkCode, UUID> {
    Optional<TelegramLinkCode> findByCode(String code);
    boolean existsByCode(String code);
    List<TelegramLinkCode> findAllByEmployee_IdAndUsedAtIsNull(UUID employeeId);
}
