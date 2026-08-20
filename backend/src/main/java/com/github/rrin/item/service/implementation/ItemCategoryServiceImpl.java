package com.github.rrin.item.service.implementation;

import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.dto.ItemCategoryRequest;
import com.github.rrin.item.repository.ItemCategoryRepository;
import com.github.rrin.item.service.ItemCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class ItemCategoryServiceImpl implements ItemCategoryService {

    private final ItemCategoryRepository categoryRepository;

    @Autowired
    public ItemCategoryServiceImpl(ItemCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ItemCategory create(ItemCategoryRequest request) {
        ItemCategory category = new ItemCategory();
        category.setName(request.getName());
        category.setChildren(Collections.emptyList());
        Optional<ItemCategory> parent = categoryRepository.findById(request.getParentId());
        parent.ifPresent(category::setParent);
        return categoryRepository.save(category);
    }

    @Override
    public ItemCategory update(ItemCategoryRequest request) {
        ItemCategory category = checkIfCategoryExists(request.getId());
        category.setName(request.getName());
        category.setParent(categoryRepository.findById(request.getParentId()).orElse(null));
        return categoryRepository.save(category);
    }

    @Override
    public ItemCategory get(UUID id) {
        return checkIfCategoryExists(id);
    }

    @Override
    public ItemCategory delete(UUID id) {
        ItemCategory category = checkIfCategoryExists(id);
        categoryRepository.delete(category);
        return category;
    }

    @Override
    public ItemCategory addChild(UUID id, UUID childId) {
        ItemCategory parent = checkIfCategoryExists(id);
        ItemCategory child = checkIfCategoryExists(childId);
        parent.addChild(child);
        return categoryRepository.save(parent);
    }

    @Override
    public ItemCategory removeChild(UUID id, UUID childId) {
        ItemCategory parent = checkIfCategoryExists(id);
        ItemCategory child = checkIfCategoryExists(childId);
        parent.removeChild(child);
        return categoryRepository.save(parent);
    }

    private ItemCategory checkIfCategoryExists(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }
}
