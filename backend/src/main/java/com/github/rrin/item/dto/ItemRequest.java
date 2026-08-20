package com.github.rrin.item.dto;

import com.github.rrin.item.ItemType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ItemRequest {
    private UUID id;
    private String name;
    private UUID categoryId;
    private ItemType type;
}
