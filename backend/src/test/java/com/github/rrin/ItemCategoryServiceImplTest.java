package com.github.rrin;

adimport com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.dto.ItemCategoryRequest;
import com.github.rrin.item.repository.ItemCategoryRepository;
import com.github.rrin.item.service.implementation.ItemCategoryServiceImpl;
import com.github.rrin.utils.PaginatedResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void getCategory() {
        UUID id = UUID.randomUUID();
        ItemCategory category = ItemCategory.builder().id(id).name("Food").build();
        when(repository.findById(id)).thenReturn(Optional.of(category));
        ItemCategory found = service.get(id);
        assertEquals(category, found);
    }

    @Test
    void getThrowsError() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(id));
    }

    @Test
    void deleteCategory() {
        UUID id = UUID.randomUUID();
        ItemCategory category = ItemCategory.builder().id(id).name("Food").build();
        when(repository.findById(id)).thenReturn(Optional.of(category));
        ItemCategory deleted = service.delete(id);
        assertEquals(category, deleted);
    }

    @Test
    void updateCategory() {
        UUID id = UUID.randomUUID();
        ItemCategory category = ItemCategory.builder().id(id).name("Food").build();
        when(repository.findById(id)).thenReturn(Optional.of(category));
        when(repository.save(any(ItemCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ItemCategory updated = service.update(ItemCategoryRequest.builder().id(id).name("Updated Food").build());
        assertEquals("Updated Food", updated.getName());
    }

    @Test
    void addChild() {
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        ItemCategory parent = ItemCategory.builder().id(parentId).name("Food").build();
        ItemCategory child = ItemCategory.builder().id(childId).name("Fruits").build();
        when(repository.findById(parentId)).thenReturn(Optional.of(parent));
        when(repository.findById(childId)).thenReturn(Optional.of(child));
        when(repository.save(any(ItemCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ItemCategory updatedParent = service.addChild(parentId, childId);
        ItemCategory updatedChild = service.get(childId);
        assertEquals(parent, updatedChild.getParent());
        assertEquals(child, updatedParent.getChildren().getFirst());
    }

    @Test
    void removeChild() {
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        ItemCategory parent = ItemCategory.builder().id(parentId).name("Food").build();
        ItemCategory child = ItemCategory.builder().id(childId).name("Fruits").build();
        parent.addChild(child);
        when(repository.findById(parentId)).thenReturn(Optional.of(parent));
        when(repository.findById(childId)).thenReturn(Optional.of(child));
        when(repository.save(any(ItemCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ItemCategory updatedParent = service.removeChild(parentId, childId);
        ItemCategory updatedChild = service.get(childId);
        assertNull(updatedChild.getParent());
        assertTrue(updatedParent.getChildren().isEmpty());
    }

    @Test
    void findByName() {
        Pageable pageable = PageRequest.of(0, 10);
        ItemCategory category = ItemCategory.builder().id(UUID.randomUUID()).name("Fruits").build();
        Page<ItemCategory> page = new PageImpl<>(List.of(category), pageable, 1);

        when(repository.findAllByNameContainingIgnoreCase("fru", pageable)).thenReturn(page);

        PaginatedResponse<ItemCategory> result = service.findByName("fru", 0, 10);

        assertEquals(1, result.getTotalItems());
        assertEquals(List.of(category), result.getData());
    }

    @Test
    void findByName_PageNumberLessThanZero() {
        assertThrows(InvalidQuery.class, () -> service.findByName("fruits", -1, 10));
    }

    @Test
    void findByName_PageSizeLessThanOne() {
        assertThrows(InvalidQuery.class, () -> service.findByName("fruits", 0, 0));
    }
}
