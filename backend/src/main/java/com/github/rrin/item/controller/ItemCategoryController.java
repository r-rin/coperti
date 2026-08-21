package com.github.rrin.item.controller;

import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.dto.ItemCategoryRequest;
import com.github.rrin.item.dto.ItemCategoryResponse;
import com.github.rrin.item.service.ItemCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/item/category")
public class ItemCategoryController {

    ItemCategoryService itemCategoryService;

    @Autowired
    public ItemCategoryController(ItemCategoryService itemCategoryService) {
        this.itemCategoryService = itemCategoryService;
    }

    @GetMapping("/{id}")
    public ItemCategoryResponse get(@PathVariable UUID id) {
        return toResponse(itemCategoryService.get(id));
    }

    @GetMapping("/{id}/children")
    public ItemCategoryResponse[] getChildren(@PathVariable UUID id) {
        return itemCategoryService.get(id).getChildren().stream()
                .map(this::toResponse)
                .toArray(ItemCategoryResponse[]::new);
    }

    @GetMapping("/{id}/parent")
    public ItemCategoryResponse getParent(@PathVariable UUID id) {
        return toResponse(itemCategoryService.get(id).getParent());
    }

    //TODO: child can't become a parent of itself, or a child of itself, it's parent (loop)
    @PostMapping("/{id}/child")
    public ItemCategoryResponse addChild(@PathVariable UUID id, @RequestBody UUID childId) {
        return toResponse(itemCategoryService.addChild(id, childId));
    }

    @DeleteMapping("/{id}/child")
    public ItemCategoryResponse removeChild(@PathVariable UUID id, @RequestBody UUID childId) {
        return toResponse(itemCategoryService.removeChild(id, childId));
    }

    @PostMapping()
    public ItemCategoryResponse create(@RequestBody ItemCategoryRequest request) {
        return toResponse(itemCategoryService.create(request));
    }

    @PutMapping()
    public ItemCategoryResponse update(@RequestBody ItemCategoryRequest request) {
        return toResponse(itemCategoryService.update(request));
    }

    @DeleteMapping("/{id}")
    public ItemCategoryResponse delete(@PathVariable UUID id) {
        return toResponse(itemCategoryService.delete(id));
    }

    // TODO: optional parameters to Object
    @PostMapping("/search")
    public ItemCategoryResponse[] search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) int page,
            @RequestParam(required = false) int size) {
        return itemCategoryService.findByName(name, page, size).getData().stream()
                .map(this::toResponse)
                .toArray(ItemCategoryResponse[]::new);
    }

    private ItemCategoryResponse toResponse(ItemCategory category) {
        return ItemCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(category.getChildren().stream().map(ItemCategory::getId).toArray(UUID[]::new))
                .build();
    }
}
