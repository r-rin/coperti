package com.github.rrin.item.service;

import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.dto.ItemCategoryRequest;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemCategoryService {
    ItemCategory create(ItemCategoryRequest request);
    ItemCategory update(ItemCategoryRequest request);
    ItemCategory get(UUID id);
    ItemCategory delete(UUID id);
    ItemCategory addChild(UUID id, UUID childId);
    ItemCategory removeChild(UUID id, UUID childId);
}
