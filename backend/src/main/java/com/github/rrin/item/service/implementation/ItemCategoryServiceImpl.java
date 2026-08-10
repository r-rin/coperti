package com.github.rrin.item.service.implementation;

import com.github.rrin.item.Item;
import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.dto.ItemCategoryRequest;
import com.github.rrin.item.repository.ItemCategoryRepository;
import com.github.rrin.item.service.ItemCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

//  TODO: add error handling

@Service
public class ItemCategoryServiceImpl implements ItemCategoryService {

    private ItemCategoryRepository categoryRepository;

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
        Optional<ItemCategory> category = categoryRepository.findById(request.getId());
        if (category.isPresent()) {
            category.get().setName(request.getName());
            category.get().setParent(categoryRepository.findById(request.getParentId()).orElse(null));
            return categoryRepository.save(category.get());
        }

        return null;
    }

    @Override
    public ItemCategory get(UUID id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public ItemCategory delete(UUID id) {
        return categoryRepository.findById(id).map(category -> {
            categoryRepository.delete(category);
            return category;
        }).orElse(null);
    }

    @Override
    public ItemCategory addChild(UUID id, UUID childId) {
        ItemCategory parent = categoryRepository.findById(id).orElse(null);
        ItemCategory child = categoryRepository.findById(childId).orElse(null);
        parent.addChild(child);
        return categoryRepository.save(parent);
    }

    @Override
    public ItemCategory removeChild(UUID id, UUID childId) {
        ItemCategory parent = categoryRepository.findById(id).orElse(null);
        ItemCategory child = categoryRepository.findById(childId).orElse(null);
        parent.removeChild(child);
        return categoryRepository.save(parent);
    }
}
