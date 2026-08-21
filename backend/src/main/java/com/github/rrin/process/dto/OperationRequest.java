package com.github.rrin.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class OperationRequest {
    private UUID id;
    private String code;
    private String name;
    private String description;
}
