package com.github.rrin.item.repository;

import com.github.rrin.item.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemCategoryRepository extends JpaRepository<ItemCategory, UUID> {
}
