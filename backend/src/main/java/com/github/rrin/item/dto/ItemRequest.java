package com.github.rrin.item.dto;

import com.github.rrin.item.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    private UUID id;
    private String name;
    private UUID categoryId;
    private ItemType type;
}
