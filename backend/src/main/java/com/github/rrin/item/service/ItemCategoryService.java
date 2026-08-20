package com.github.rrin.item.service;

import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.dto.ItemCategoryRequest;
import com.github.rrin.utils.PaginatedResponse;

import java.util.UUID;

public interface ItemCategoryService {
    ItemCategory create(ItemCategoryRequest request);
    ItemCategory update(ItemCategoryRequest request);
    ItemCategory get(UUID id);
    ItemCategory delete(UUID id);
    ItemCategory addChild(UUID id, UUID childId);
    ItemCategory removeChild(UUID id, UUID childId);

    PaginatedResponse<ItemCategory> findByName(String name, int page, int size);
}
