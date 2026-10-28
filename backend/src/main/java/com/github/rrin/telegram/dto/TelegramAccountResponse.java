package com.github.rrin.telegram.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramAccountResponse {
    private UUID id;
    private UUID employeeId;
    private Long telegramUserId;
    private Instant linkedAt;
}
