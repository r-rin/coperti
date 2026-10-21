package com.github.rrin.inventory.dto;

import com.github.rrin.inventory.LocationKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private UUID id;
    private String code;
    private String name;
    private LocationKind kind;
    private UUID employeeId;
}
