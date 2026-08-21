package com.github.rrin.item.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.dto.ItemCategoryRequest;
import com.github.rrin.item.repository.ItemCategoryRepository;
import com.github.rrin.item.service.ItemCategoryService;
import com.github.rrin.utils.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
        category.setParent(resolveParent(request.getParentId()));
        return categoryRepository.save(category);
    }

    @Override
    public ItemCategory update(ItemCategoryRequest request) {
        ItemCategory category = checkIfCategoryExists(request.getId());
        category.setName(request.getName());
        category.setParent(resolveParent(request.getParentId()));
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
        Optional<ItemCategory> parentOptional = categoryRepository.findById(id);
        Optional<ItemCategory> childOptional = categoryRepository.findById(childId);

        new ValidationCheck()
                .check(parentOptional.isPresent(), "Parent category not found")
                .check(childOptional.isPresent(), "Child category not found")
                .throwIfAny(EntityNotFoundException::new);

        ItemCategory parent = parentOptional.orElseThrow(EntityNotFoundException::new);
        ItemCategory child = childOptional.orElseThrow(EntityNotFoundException::new);
        parent.addChild(child);
        return categoryRepository.save(parent);
    }

    @Override
    public ItemCategory removeChild(UUID id, UUID childId) {
        Optional<ItemCategory> parentOptional = categoryRepository.findById(id);
        Optional<ItemCategory> childOptional = categoryRepository.findById(childId);

        new ValidationCheck()
                .check(parentOptional.isPresent(), "Parent category not found")
                .check(childOptional.isPresent(), "Child category not found")
                .throwIfAny(EntityNotFoundException::new);

        ItemCategory parent = parentOptional.orElseThrow(EntityNotFoundException::new);
        ItemCategory child = childOptional.orElseThrow(EntityNotFoundException::new);
        parent.removeChild(child);
        return categoryRepository.save(parent);
    }

    @Override
    public PaginatedResponse<ItemCategory> findByName(String name, int page, int size) {
        new ValidationCheck()
                .check(page >= 0, "Page number must be greater than or equal to 0")
                .check(size > 0, "Page size must be greater than 0")
                .throwIfAny(InvalidQuery::new);


        return PaginatedResponse.of(
                categoryRepository.findAllByNameContainingIgnoreCase(name, PageRequest.of(page, size))
        );
    }

    private ItemCategory resolveParent(UUID parentId) {
        if (parentId == null) {
            return null;
        }
        return categoryRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent category not found with id: " + parentId));
    }

    private ItemCategory checkIfCategoryExists(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }
}
