package com.github.rrin.inventory.dto.filter;

import com.github.rrin.inventory.OtherUseStatus;
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
public class OtherUseFilter {
    private OtherUseStatus status;
    private UUID requestedById;
    // bounds on when the request was made
    private Instant from;
    private Instant to;
}
