package com.github.rrin.item.service;

import com.github.rrin.item.Item;
import com.github.rrin.item.ItemType;
import com.github.rrin.item.dto.ItemRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemService {
    Item create(ItemRequest itemRequest);
    Item update(ItemRequest itemRequest);
    Item get(UUID id);
    Item delete(UUID id);

    Item addItemToCategory(UUID itemId, UUID categoryId);
    Item removeItemFromCategory(UUID itemId, UUID categoryId);
    Item setItemType(UUID itemId, ItemType type);

    List<Item> getItemsByCategory(UUID categoryId);
    List<Item> getItemsByType(ItemType type);
}
