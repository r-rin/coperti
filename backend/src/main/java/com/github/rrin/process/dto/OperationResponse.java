package com.github.rrin.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
}
