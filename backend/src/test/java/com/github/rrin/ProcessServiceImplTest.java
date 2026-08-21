package com.github.rrin;

import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.item.Item;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.process.Process;
import com.github.rrin.process.ProcessStatus;
import com.github.rrin.process.ProcessStep;
import com.github.rrin.process.dto.ProcessRequest;
import com.github.rrin.process.dto.ProcessStepRequest;
import com.github.rrin.process.repository.ProcessRepository;
import com.github.rrin.process.service.ProcessStepService;
import com.github.rrin.process.service.implementation.ProcessServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
class ProcessServiceImplTest {

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProcessStepService processStepService;

    @InjectMocks
    private ProcessServiceImpl service;

    @Test
    void getProcess() {
        UUID id = UUID.randomUUID();
        Process process = Process.builder().id(id).steps(List.of()).build();
        when(processRepository.findById(id)).thenReturn(Optional.of(process));
        assertEquals(process, service.get(id));
    }

    @Test
    void getThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(processRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(id));
    }

    @Test
    void createProcess() {
        UUID itemId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        Item item = Item.builder().id(itemId).name("Vacuum cleaner").build();
        Process persisted = Process.builder()
                .id(processId)
                .produces(item)
                .version(1)
                .status(ProcessStatus.DRAFT)
                .steps(List.of(ProcessStep.builder().id(UUID.randomUUID()).seq(1).build()))
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> {
            Process process = invocation.getArgument(0);
            process.setId(processId);
            return process;
        });
        when(processRepository.findById(processId)).thenReturn(Optional.of(persisted));

        ProcessStepRequest stepRequest1 = ProcessStepRequest.builder().seq(1).outputQuantity(1).build();
        ProcessStepRequest stepRequest2 = ProcessStepRequest.builder().seq(2).outputQuantity(1).build();

        Process created = service.create(ProcessRequest.builder()
                .producedItemId(itemId)
                .version(1)
                .status(ProcessStatus.DRAFT)
                .steps(List.of(stepRequest1, stepRequest2))
                .build());

        verify(processStepService).create(processId, stepRequest1);
        verify(processStepService).create(processId, stepRequest2);
        assertEquals(persisted, created);
    }

    @Test
    void createDefaultsStatusToDraftWhenNull() {
        UUID itemId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        Item item = Item.builder().id(itemId).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> {
            Process process = invocation.getArgument(0);
            process.setId(processId);
            assertEquals(ProcessStatus.DRAFT, process.getStatus());
            return process;
        });
        when(processRepository.findById(processId))
                .thenAnswer(invocation -> Optional.of(Process.builder().id(processId).steps(List.of()).build()));

        service.create(ProcessRequest.builder()
                .producedItemId(itemId)
                .version(1)
                .build());
    }

    @Test
    void createIgnoresRequestedStatusAndStartsAsDraft() {
        UUID itemId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        Item item = Item.builder().id(itemId).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> {
            Process process = invocation.getArgument(0);
            process.setId(processId);
            assertEquals(ProcessStatus.DRAFT, process.getStatus());
            return process;
        });
        when(processRepository.findById(processId))
                .thenAnswer(invocation -> Optional.of(Process.builder().id(processId).steps(List.of()).build()));

        service.create(ProcessRequest.builder()
                .producedItemId(itemId)
                .version(1)
                .status(ProcessStatus.ACTIVE)
                .build());
    }

    @Test
    void createThrowsWhenVersionAlreadyExistsForItem() {
        UUID itemId = UUID.randomUUID();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(Item.builder().id(itemId).build()));
        when(processRepository.findByProduces_IdAndVersion(itemId, 1))
                .thenReturn(Optional.of(Process.builder().id(UUID.randomUUID()).version(1).build()));

        assertThrows(InvalidQuery.class, () -> service.create(ProcessRequest.builder()
                .producedItemId(itemId)
                .version(1)
                .build()));
        verify(processRepository, never()).save(any());
    }

    @Test
    void createWithNoSteps() {
        UUID itemId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        Item item = Item.builder().id(itemId).build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> {
            Process process = invocation.getArgument(0);
            process.setId(processId);
            return process;
        });
        when(processRepository.findById(processId))
                .thenReturn(Optional.of(Process.builder().id(processId).steps(List.of()).build()));

        service.create(ProcessRequest.builder()
                .producedItemId(itemId)
                .version(1)
                .status(ProcessStatus.DRAFT)
                .build());

        verify(processStepService, never()).create(any(), any());
    }

    @Test
    void createThrowsWhenProducedItemNotFound() {
        UUID itemId = UUID.randomUUID();
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.create(ProcessRequest.builder()
                .producedItemId(itemId)
                .version(1)
                .build()));
    }

    @Test
    void createThrowsWhenVersionNotPositive() {
        UUID itemId = UUID.randomUUID();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(Item.builder().id(itemId).build()));

        assertThrows(InvalidQuery.class, () -> service.create(ProcessRequest.builder()
                .producedItemId(itemId)
                .version(0)
                .build()));
    }

    @Test
    void updateProcess() {
        UUID id = UUID.randomUUID();
        UUID newItemId = UUID.randomUUID();
        Item newItem = Item.builder().id(newItemId).name("Blender").build();
        List<ProcessStep> existingSteps = List.of(ProcessStep.builder().id(UUID.randomUUID()).build());
        Process process = Process.builder()
                .id(id)
                .produces(Item.builder().id(UUID.randomUUID()).build())
                .version(1)
                .status(ProcessStatus.DRAFT)
                .steps(existingSteps)
                .build();

        when(processRepository.findById(id)).thenReturn(Optional.of(process));
        when(itemRepository.findById(newItemId)).thenReturn(Optional.of(newItem));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Process updated = service.update(ProcessRequest.builder()
                .id(id)
                .producedItemId(newItemId)
                .version(2)
                .status(ProcessStatus.ARCHIVED)
                .build());

        assertEquals(newItem, updated.getProduces());
        assertEquals(2, updated.getVersion());
        assertEquals(ProcessStatus.DRAFT, updated.getStatus());
        assertEquals(existingSteps, updated.getSteps());
    }

    @Test
    void updateThrowsWhenNotDraft() {
        UUID id = UUID.randomUUID();
        Process process = Process.builder()
                .id(id)
                .produces(Item.builder().id(UUID.randomUUID()).build())
                .version(1)
                .status(ProcessStatus.ACTIVE)
                .steps(List.of())
                .build();
        when(processRepository.findById(id)).thenReturn(Optional.of(process));

        assertThrows(InvalidQuery.class, () -> service.update(ProcessRequest.builder()
                .id(id)
                .producedItemId(UUID.randomUUID())
                .version(2)
                .build()));
        verify(processRepository, never()).save(any());
    }

    @Test
    void updateThrowsWhenVersionTakenByAnotherProcess() {
        UUID id = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(processRepository.findById(id))
                .thenReturn(Optional.of(Process.builder().id(id).status(ProcessStatus.DRAFT).steps(List.of()).build()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(Item.builder().id(itemId).build()));
        when(processRepository.findByProduces_IdAndVersion(itemId, 3))
                .thenReturn(Optional.of(Process.builder().id(UUID.randomUUID()).version(3).build()));

        assertThrows(InvalidQuery.class, () -> service.update(ProcessRequest.builder()
                .id(id)
                .producedItemId(itemId)
                .version(3)
                .build()));
        verify(processRepository, never()).save(any());
    }

    @Test
    void updateAllowsKeepingOwnVersion() {
        UUID id = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        Process process = Process.builder()
                .id(id)
                .status(ProcessStatus.DRAFT)
                .produces(Item.builder().id(itemId).build())
                .version(1)
                .steps(List.of())
                .build();
        when(processRepository.findById(id)).thenReturn(Optional.of(process));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(Item.builder().id(itemId).build()));
        when(processRepository.findByProduces_IdAndVersion(itemId, 1)).thenReturn(Optional.of(process));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(1, service.update(ProcessRequest.builder()
                .id(id)
                .producedItemId(itemId)
                .version(1)
                .build()).getVersion());
    }

    @Test
    void updateThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(processRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(ProcessRequest.builder()
                .id(id)
                .producedItemId(UUID.randomUUID())
                .version(1)
                .build()));
    }

    @Test
    void updateThrowsWhenProducedItemNotFound() {
        UUID id = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(processRepository.findById(id))
                .thenReturn(Optional.of(Process.builder().id(id).status(ProcessStatus.DRAFT).steps(List.of()).build()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(ProcessRequest.builder()
                .id(id)
                .producedItemId(itemId)
                .version(1)
                .build()));
    }

    @Test
    void updateThrowsWhenVersionNotPositive() {
        UUID id = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(processRepository.findById(id))
                .thenReturn(Optional.of(Process.builder().id(id).status(ProcessStatus.DRAFT).steps(List.of()).build()));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(Item.builder().id(itemId).build()));

        assertThrows(InvalidQuery.class, () -> service.update(ProcessRequest.builder()
                .id(id)
                .producedItemId(itemId)
                .version(-1)
                .build()));
    }

    @Test
    void deleteProcess() {
        UUID id = UUID.randomUUID();
        ProcessStep step1 = ProcessStep.builder().id(UUID.randomUUID()).seq(1).build();
        ProcessStep step2 = ProcessStep.builder().id(UUID.randomUUID()).seq(2).build();
        Process process = Process.builder().id(id).status(ProcessStatus.DRAFT).steps(List.of(step1, step2)).build();

        when(processRepository.findById(id)).thenReturn(Optional.of(process));

        Process deleted = service.delete(id);

        InOrder inOrder = Mockito.inOrder(processStepService, processRepository);
        inOrder.verify(processStepService).delete(step1.getId());
        inOrder.verify(processStepService).delete(step2.getId());
        inOrder.verify(processRepository).delete(process);
        assertEquals(process, deleted);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(processRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.delete(id));
    }

    @Test
    void deleteThrowsWhenNotDraft() {
        UUID id = UUID.randomUUID();
        Process process = Process.builder().id(id).status(ProcessStatus.ACTIVE).steps(List.of()).build();
        when(processRepository.findById(id)).thenReturn(Optional.of(process));

        assertThrows(InvalidQuery.class, () -> service.delete(id));
        verify(processRepository, never()).delete(any());
    }

    @Test
    void setDraftedSetsStatus() {
        UUID id = UUID.randomUUID();
        Process process = Process.builder().id(id).status(ProcessStatus.ARCHIVED).steps(List.of()).build();
        when(processRepository.findById(id)).thenReturn(Optional.of(process));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(ProcessStatus.DRAFT, service.setDrafted(id).getStatus());
    }

    @Test
    void setDraftedThrowsWhenActive() {
        UUID id = UUID.randomUUID();
        Process process = Process.builder().id(id).status(ProcessStatus.ACTIVE).steps(List.of()).build();
        when(processRepository.findById(id)).thenReturn(Optional.of(process));

        assertThrows(InvalidQuery.class, () -> service.setDrafted(id));
        verify(processRepository, never()).save(any());
    }

    @Test
    void setActiveSetsStatusEvenWhenArchived() {
        UUID id = UUID.randomUUID();
        Process process = Process.builder()
                .id(id)
                .status(ProcessStatus.ARCHIVED)
                .produces(Item.builder().id(UUID.randomUUID()).build())
                .steps(List.of(ProcessStep.builder().id(UUID.randomUUID()).seq(1).build()))
                .build();
        when(processRepository.findById(id)).thenReturn(Optional.of(process));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(ProcessStatus.ACTIVE, service.setActive(id).getStatus());
    }

    @Test
    void setActiveThrowsWhenProcessHasNoSteps() {
        UUID id = UUID.randomUUID();
        Process process = Process.builder()
                .id(id)
                .status(ProcessStatus.DRAFT)
                .produces(Item.builder().id(UUID.randomUUID()).build())
                .steps(List.of())
                .build();
        when(processRepository.findById(id)).thenReturn(Optional.of(process));

        assertThrows(InvalidQuery.class, () -> service.setActive(id));
        verify(processRepository, never()).save(any());
    }

    @Test
    void setActiveArchivesCurrentlyActiveProcessForSameItem() {
        UUID itemId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Item item = Item.builder().id(itemId).build();
        Process currentlyActive = Process.builder()
                .id(UUID.randomUUID())
                .status(ProcessStatus.ACTIVE)
                .produces(item)
                .version(1)
                .steps(List.of(ProcessStep.builder().id(UUID.randomUUID()).seq(1).build()))
                .build();
        Process process = Process.builder()
                .id(id)
                .status(ProcessStatus.DRAFT)
                .produces(item)
                .version(2)
                .steps(List.of(ProcessStep.builder().id(UUID.randomUUID()).seq(1).build()))
                .build();

        when(processRepository.findById(id)).thenReturn(Optional.of(process));
        when(processRepository.findFirstByProduces_IdAndStatus(itemId, ProcessStatus.ACTIVE))
                .thenReturn(Optional.of(currentlyActive));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Process activated = service.setActive(id);

        assertEquals(ProcessStatus.ACTIVE, activated.getStatus());
        assertEquals(ProcessStatus.ARCHIVED, currentlyActive.getStatus());
        verify(processRepository).save(currentlyActive);
    }

    @Test
    void setActiveIsNoOpWhenAlreadyActive() {
        UUID id = UUID.randomUUID();
        Process process = Process.builder()
                .id(id)
                .status(ProcessStatus.ACTIVE)
                .produces(Item.builder().id(UUID.randomUUID()).build())
                .steps(List.of(ProcessStep.builder().id(UUID.randomUUID()).seq(1).build()))
                .build();
        when(processRepository.findById(id)).thenReturn(Optional.of(process));

        assertEquals(process, service.setActive(id));
        verify(processRepository, never()).save(any());
    }

    @Test
    void setArchivedSetsStatus() {
        UUID id = UUID.randomUUID();
        Process process = Process.builder().id(id).status(ProcessStatus.ACTIVE).steps(List.of()).build();
        when(processRepository.findById(id)).thenReturn(Optional.of(process));
        when(processRepository.save(any(Process.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(ProcessStatus.ARCHIVED, service.setArchived(id).getStatus());
    }

    @Test
    void setDraftedThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(processRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.setDrafted(id));
    }

    @Test
    void setActiveThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(processRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.setActive(id));
    }

    @Test
    void setArchivedThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(processRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.setArchived(id));
    }
}
