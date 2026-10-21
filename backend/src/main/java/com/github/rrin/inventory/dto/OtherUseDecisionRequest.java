package com.github.rrin.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtherUseDecisionRequest {
    // whoever approves or rejects; stored as approvedBy either way
    private UUID employeeId;
}
