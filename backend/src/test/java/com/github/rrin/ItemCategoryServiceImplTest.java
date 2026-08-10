package com.github.rrin;

import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.dto.ItemCategoryRequest;
import com.github.rrin.item.repository.ItemCategoryRepository;
import com.github.rrin.item.service.ItemCategoryService;
import com.github.rrin.item.service.implementation.ItemCategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemCategoryServiceImplTest {

    @Mock
    private ItemCategoryRepository repository;

    @InjectMocks
    private ItemCategoryServiceImpl service;

    @Test
    void createCategory() {
        UUID parentId = UUID.randomUUID();
        ItemCategory parent = ItemCategory.builder().id(parentId).name("Food").build();

        ItemCategoryRequest request = ItemCategoryRequest.builder()
                .name("Fruits")
                .parentId(parentId)
                .build();

        when(repository.findById(parentId)).thenReturn(Optional.of(parent));
        when(repository.save(any(ItemCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ItemCategory created = service.create(request);

        assertEquals("Fruits", created.getName());
        assertEquals(parent, created.getParent());
    }
}
