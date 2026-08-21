package com.github.rrin;

import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.item.Item;
import com.github.rrin.item.ItemCategory;
import com.github.rrin.item.repository.ItemCategoryRepository;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.process.Process;
import com.github.rrin.process.ProcessComponent;
import com.github.rrin.process.ProcessStatus;
import com.github.rrin.process.ProcessStep;
import com.github.rrin.process.dto.ProcessComponentRequest;
import com.github.rrin.process.repository.ProcessComponentRepository;
import com.github.rrin.process.repository.ProcessStepRepository;
import com.github.rrin.process.service.implementation.ProcessComponentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessComponentServiceImplTest {

    @Mock
    private ProcessComponentRepository componentRepository;

    @Mock
    private ProcessStepRepository stepRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemCategoryRepository categoryRepository;

    @InjectMocks
    private ProcessComponentServiceImpl service;

    private ProcessStep draftStep(UUID stepId) {
        Process process = Process.builder().id(UUID.randomUUID()).status(ProcessStatus.DRAFT).build();
        return ProcessStep.builder().id(stepId).process(process).build();
    }

    @Test
    void createWithItemOnly() {
        UUID stepId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        ProcessStep step = draftStep(stepId);
        Item item = Item.builder().id(itemId).name("Motor").build();

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(step));
        when(itemRepository.existsById(itemId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(componentRepository.save(any(ProcessComponent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProcessComponent created = service.create(stepId, ProcessComponentRequest.builder()
                .consumedItemId(itemId)
                .consumedQuantity(4)
                .build());

        assertEquals(step, created.getProcessStep());
        assertEquals(item, created.getConsumed());
        assertNull(created.getConsumableCategory());
        assertEquals(4, created.getConsumedQuantity());
    }

    @Test
    void createWithCategoryOnly() {
        UUID stepId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        ProcessStep step = draftStep(stepId);
        ItemCategory category = ItemCategory.builder().id(categoryId).name("Motor 24V").build();

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(step));
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(componentRepository.save(any(ProcessComponent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProcessComponent created = service.create(stepId, ProcessComponentRequest.builder()
                .consumableCategoryId(categoryId)
                .consumedQuantity(1)
                .build());

        assertNull(created.getConsumed());
        assertEquals(category, created.getConsumableCategory());
    }

    @Test
    void createThrowsWhenBothItemAndCategorySet() {
        UUID stepId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(draftStep(stepId)));

        assertThrows(InvalidQuery.class, () -> service.create(stepId, ProcessComponentRequest.builder()
                .consumedItemId(UUID.randomUUID())
                .consumableCategoryId(UUID.randomUUID())
                .consumedQuantity(1)
                .build()));
    }

    @Test
    void createThrowsWhenNeitherItemNorCategorySet() {
        UUID stepId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(draftStep(stepId)));

        assertThrows(InvalidQuery.class, () -> service.create(stepId, ProcessComponentRequest.builder()
                .consumedQuantity(1)
                .build()));
    }

    @Test
    void createThrowsWhenQuantityNotPositive() {
        UUID stepId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(draftStep(stepId)));

        assertThrows(InvalidQuery.class, () -> service.create(stepId, ProcessComponentRequest.builder()
                .consumedItemId(UUID.randomUUID())
                .consumedQuantity(0)
                .build()));
    }

    @Test
    void createThrowsWhenProcessStepNotFound() {
        UUID stepId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.create(stepId, ProcessComponentRequest.builder()
                .consumedItemId(UUID.randomUUID())
                .consumedQuantity(1)
                .build()));
    }

    @Test
    void createThrowsWhenConsumedItemNotFound() {
        UUID stepId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(draftStep(stepId)));
        when(itemRepository.existsById(itemId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.create(stepId, ProcessComponentRequest.builder()
                .consumedItemId(itemId)
                .consumedQuantity(1)
                .build()));
    }

    @Test
    void createThrowsWhenConsumableCategoryNotFound() {
        UUID stepId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(draftStep(stepId)));
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.create(stepId, ProcessComponentRequest.builder()
                .consumableCategoryId(categoryId)
                .consumedQuantity(1)
                .build()));
    }

    @Test
    void updateComponent() {
        UUID id = UUID.randomUUID();
        UUID newItemId = UUID.randomUUID();
        Item newItem = Item.builder().id(newItemId).name("Frame").build();
        ProcessComponent component = ProcessComponent.builder()
                .id(id)
                .processStep(draftStep(UUID.randomUUID()))
                .consumableCategory(ItemCategory.builder().id(UUID.randomUUID()).build())
                .consumedQuantity(2)
                .build();

        when(componentRepository.findById(id)).thenReturn(Optional.of(component));
        when(itemRepository.existsById(newItemId)).thenReturn(true);
        when(itemRepository.findById(newItemId)).thenReturn(Optional.of(newItem));
        when(componentRepository.save(any(ProcessComponent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProcessComponent updated = service.update(ProcessComponentRequest.builder()
                .id(id)
                .consumedItemId(newItemId)
                .consumedQuantity(6)
                .build());

        assertEquals(newItem, updated.getConsumed());
        assertNull(updated.getConsumableCategory());
        assertEquals(6, updated.getConsumedQuantity());
    }

    @Test
    void updateThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(componentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(ProcessComponentRequest.builder()
                .id(id)
                .consumedItemId(UUID.randomUUID())
                .consumedQuantity(1)
                .build()));
    }

    @Test
    void updateThrowsWhenBothOrNeitherSet() {
        UUID id = UUID.randomUUID();
        ProcessComponent component = ProcessComponent.builder()
                .id(id)
                .processStep(draftStep(UUID.randomUUID()))
                .build();
        when(componentRepository.findById(id)).thenReturn(Optional.of(component));

        assertThrows(InvalidQuery.class, () -> service.update(ProcessComponentRequest.builder()
                .id(id)
                .consumedItemId(UUID.randomUUID())
                .consumableCategoryId(UUID.randomUUID())
                .consumedQuantity(1)
                .build()));
        assertThrows(InvalidQuery.class, () -> service.update(ProcessComponentRequest.builder()
                .id(id)
                .consumedQuantity(1)
                .build()));
    }

    @Test
    void getComponent() {
        UUID id = UUID.randomUUID();
        ProcessComponent component = ProcessComponent.builder().id(id).build();
        when(componentRepository.findById(id)).thenReturn(Optional.of(component));
        assertEquals(component, service.get(id));
    }

    @Test
    void getThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(componentRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(id));
    }

    @Test
    void deleteComponent() {
        UUID id = UUID.randomUUID();
        ProcessComponent component = ProcessComponent.builder()
                .id(id)
                .processStep(draftStep(UUID.randomUUID()))
                .build();
        when(componentRepository.findById(id)).thenReturn(Optional.of(component));

        ProcessComponent deleted = service.delete(id);

        verify(componentRepository).delete(component);
        assertEquals(component, deleted);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(componentRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.delete(id));
    }

    @Test
    void deleteAllForStepReturnsAndRemovesAllComponents() {
        UUID stepId = UUID.randomUUID();
        List<ProcessComponent> components = List.of(
                ProcessComponent.builder().id(UUID.randomUUID()).build(),
                ProcessComponent.builder().id(UUID.randomUUID()).build()
        );
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(draftStep(stepId)));
        when(componentRepository.findAllByProcessStep_Id(stepId)).thenReturn(components);

        List<ProcessComponent> deleted = service.deleteAllForStep(stepId);

        verify(componentRepository).deleteAll(components);
        assertEquals(components, deleted);
    }

    @Test
    void deleteAllForStepReturnsEmptyListWhenNoneFound() {
        UUID stepId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(draftStep(stepId)));
        when(componentRepository.findAllByProcessStep_Id(stepId)).thenReturn(List.of());

        List<ProcessComponent> deleted = service.deleteAllForStep(stepId);

        verify(componentRepository).deleteAll(List.of());
        assertTrue(deleted.isEmpty());
    }

    @Test
    void deleteAllForStepThrowsWhenStepNotFound() {
        UUID stepId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.deleteAllForStep(stepId));
    }

    @Test
    void getAllForStepReturnsComponents() {
        UUID stepId = UUID.randomUUID();
        List<ProcessComponent> components = List.of(ProcessComponent.builder().id(UUID.randomUUID()).build());
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(draftStep(stepId)));
        when(componentRepository.findAllByProcessStep_Id(stepId)).thenReturn(components);

        assertEquals(components, service.getAllForStep(stepId));
    }

    @Test
    void getAllForStepThrowsWhenStepNotFound() {
        UUID stepId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getAllForStep(stepId));
    }

    @Test
    void createThrowsWhenProcessNotDraft() {
        UUID stepId = UUID.randomUUID();
        ProcessStep step = draftStep(stepId);
        step.getProcess().setStatus(ProcessStatus.ACTIVE);
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(step));

        assertThrows(InvalidQuery.class, () -> service.create(stepId, ProcessComponentRequest.builder()
                .consumedItemId(UUID.randomUUID())
                .consumedQuantity(1)
                .build()));
        verify(componentRepository, never()).save(any());
    }

    @Test
    void updateThrowsWhenProcessNotDraft() {
        UUID id = UUID.randomUUID();
        ProcessStep step = draftStep(UUID.randomUUID());
        step.getProcess().setStatus(ProcessStatus.ARCHIVED);
        ProcessComponent component = ProcessComponent.builder().id(id).processStep(step).build();
        when(componentRepository.findById(id)).thenReturn(Optional.of(component));

        assertThrows(InvalidQuery.class, () -> service.update(ProcessComponentRequest.builder()
                .id(id)
                .consumedItemId(UUID.randomUUID())
                .consumedQuantity(1)
                .build()));
        verify(componentRepository, never()).save(any());
    }

    @Test
    void deleteThrowsWhenProcessNotDraft() {
        UUID id = UUID.randomUUID();
        ProcessStep step = draftStep(UUID.randomUUID());
        step.getProcess().setStatus(ProcessStatus.ACTIVE);
        ProcessComponent component = ProcessComponent.builder().id(id).processStep(step).build();
        when(componentRepository.findById(id)).thenReturn(Optional.of(component));

        assertThrows(InvalidQuery.class, () -> service.delete(id));
        verify(componentRepository, never()).delete(any());
    }
}
