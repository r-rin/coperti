package com.github.rrin.item.controller;

import com.github.rrin.item.Item;
import com.github.rrin.item.ItemType;
import com.github.rrin.item.dto.ItemRequest;
import com.github.rrin.item.dto.ItemResponse;
import com.github.rrin.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/item")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    public ItemResponse get(@PathVariable UUID id) {
        return toResponse(itemService.get(id));
    }

    @PostMapping
    public ItemResponse create(@RequestBody ItemRequest request) {
        return toResponse(itemService.create(request));
    }

    @PutMapping
    public ItemResponse update(@RequestBody ItemRequest request) {
        return toResponse(itemService.update(request));
    }

    @DeleteMapping("/{id}")
    public ItemResponse delete(@PathVariable UUID id) {
        return toResponse(itemService.delete(id));
    }

    @PostMapping("/{id}/category")
    public ItemResponse addToCategory(@PathVariable UUID id, @RequestBody UUID categoryId) {
        return toResponse(itemService.addItemToCategory(id, categoryId));
    }

    @DeleteMapping("/{id}/category")
    public ItemResponse removeFromCategory(@PathVariable UUID id, @RequestBody UUID categoryId) {
        return toResponse(itemService.removeItemFromCategory(id, categoryId));
    }

    @PostMapping("/{id}/type")
    public ItemResponse setType(@PathVariable UUID id, @RequestBody ItemType type) {
        return toResponse(itemService.setItemType(id, type));
    }

    @GetMapping("/by-category/{categoryId}")
    public List<ItemResponse> getByCategory(@PathVariable UUID categoryId) {
        return itemService.getItemsByCategory(categoryId).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/by-type/{type}")
    public List<ItemResponse> getByType(@PathVariable ItemType type) {
        return itemService.getItemsByType(type).stream()
                .map(this::toResponse)
                .toList();
    }

    private ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .type(item.getType())
                .build();
    }
}
