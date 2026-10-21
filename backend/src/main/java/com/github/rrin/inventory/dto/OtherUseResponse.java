package com.github.rrin.inventory.dto;

import com.github.rrin.inventory.OtherUseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtherUseResponse {
    private UUID id;
    private UUID requestedById;
    private UUID approvedById;
    private String reason;
    private Instant requestedAt;
    private Instant occurredAt;
    private OtherUseStatus status;
    private List<OtherUseLineResponse> lines;
}
