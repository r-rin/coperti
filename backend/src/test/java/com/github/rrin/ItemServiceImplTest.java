package com.github.rrin;

import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.item.Item;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.item.service.ItemCategoryService;
import com.github.rrin.item.service.implementation.ItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemCategoryService itemCategoryService;

    @InjectMocks
    private ItemServiceImpl service;

    @Test
    void searchMatchesTrimmedName() {
        Item screw = Item.builder().id(UUID.randomUUID()).name("Screw M4").build();
        when(itemRepository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc("scr")).thenReturn(List.of(screw));

        assertEquals(List.of(screw), service.search("  scr "));
    }

    @Test
    void searchRejectsBlankText() {
        assertThrows(InvalidQuery.class, () -> service.search("  "));
        assertThrows(InvalidQuery.class, () -> service.search(null));
        verify(itemRepository, never()).findTop20ByNameContainingIgnoreCaseOrderByNameAsc(any());
    }
}
