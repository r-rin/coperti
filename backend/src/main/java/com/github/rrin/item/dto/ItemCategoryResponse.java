package com.github.rrin.item.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ItemCategoryResponse {
    private UUID id;
    private String name;
    private UUID parentId;
    private UUID[] children;
}
