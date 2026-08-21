package com.github.rrin;

import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.InvalidQuery;
import com.github.rrin.item.Item;
import com.github.rrin.item.repository.ItemRepository;
import com.github.rrin.process.Operation;
import com.github.rrin.process.Process;
import com.github.rrin.process.ProcessStatus;
import com.github.rrin.process.ProcessStep;
import com.github.rrin.process.dto.ProcessComponentRequest;
import com.github.rrin.process.dto.ProcessStepRequest;
import com.github.rrin.process.repository.OperationRepository;
import com.github.rrin.process.repository.ProcessRepository;
import com.github.rrin.process.repository.ProcessStepRepository;
import com.github.rrin.process.service.ProcessComponentService;
import com.github.rrin.process.service.implementation.ProcessStepServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessStepServiceImplTest {

    @Mock
    private ProcessStepRepository stepRepository;

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProcessComponentService componentService;

    @InjectMocks
    private ProcessStepServiceImpl service;

    private Process emptyProcess(UUID processId) {
        return Process.builder().id(processId).status(ProcessStatus.DRAFT).steps(new ArrayList<>()).build();
    }

    @Test
    void createStep() {
        UUID processId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        UUID outputItemId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();
        Operation operation = Operation.builder().id(operationId).code("WELD").build();
        Item outputItem = Item.builder().id(outputItemId).name("Frame").build();

        when(processRepository.findById(processId)).thenReturn(Optional.of(emptyProcess(processId)));
        when(operationRepository.findById(operationId)).thenReturn(Optional.of(operation));
        when(itemRepository.findById(outputItemId)).thenReturn(Optional.of(outputItem));
        when(stepRepository.save(any(ProcessStep.class))).thenAnswer(invocation -> {
            ProcessStep step = invocation.getArgument(0);
            step.setId(stepId);
            return step;
        });

        ProcessComponentRequest componentRequest1 = ProcessComponentRequest.builder()
                .consumedItemId(UUID.randomUUID()).consumedQuantity(2).build();
        ProcessComponentRequest componentRequest2 = ProcessComponentRequest.builder()
                .consumableCategoryId(UUID.randomUUID()).consumedQuantity(1).build();

        ProcessStep created = service.create(processId, ProcessStepRequest.builder()
                .seq(1)
                .operationId(operationId)
                .outputItemId(outputItemId)
                .outputQuantity(1)
                .components(List.of(componentRequest1, componentRequest2))
                .build());

        assertEquals(1, created.getSeq());
        assertEquals(operation, created.getOperation());
        assertEquals(outputItem, created.getOutputItem());
        assertEquals(1, created.getOutputQuantity());
        verify(componentService).create(stepId, componentRequest1);
        verify(componentService).create(stepId, componentRequest2);
    }

    @Test
    void createStepWithNoOutputItem() {
        UUID processId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();

        when(processRepository.findById(processId)).thenReturn(Optional.of(emptyProcess(processId)));
        when(operationRepository.findById(operationId))
                .thenReturn(Optional.of(Operation.builder().id(operationId).build()));
        when(stepRepository.save(any(ProcessStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessStep created = service.create(processId, ProcessStepRequest.builder()
                .seq(1)
                .operationId(operationId)
                .outputQuantity(1)
                .build());

        assertNull(created.getOutputItem());
    }

    @Test
    void createStepWithNoComponents() {
        UUID processId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();

        when(processRepository.findById(processId)).thenReturn(Optional.of(emptyProcess(processId)));
        when(operationRepository.findById(operationId))
                .thenReturn(Optional.of(Operation.builder().id(operationId).build()));
        when(stepRepository.save(any(ProcessStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(processId, ProcessStepRequest.builder()
                .seq(1)
                .operationId(operationId)
                .outputQuantity(1)
                .build());

        verify(componentService, never()).create(any(), any());
    }

    @Test
    void createThrowsWhenProcessNotFound() {
        UUID processId = UUID.randomUUID();
        when(processRepository.findById(processId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.create(processId, ProcessStepRequest.builder().build()));
    }

    @Test
    void createThrowsWhenOperationNotFound() {
        UUID processId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        when(processRepository.findById(processId)).thenReturn(Optional.of(emptyProcess(processId)));
        when(operationRepository.findById(operationId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.create(processId, ProcessStepRequest.builder()
                .operationId(operationId)
                .build()));
    }

    @Test
    void createThrowsWhenOutputItemNotFound() {
        UUID processId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        UUID outputItemId = UUID.randomUUID();
        when(processRepository.findById(processId)).thenReturn(Optional.of(emptyProcess(processId)));
        when(operationRepository.findById(operationId))
                .thenReturn(Optional.of(Operation.builder().id(operationId).build()));
        when(itemRepository.findById(outputItemId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.create(processId, ProcessStepRequest.builder()
                .operationId(operationId)
                .outputItemId(outputItemId)
                .build()));
    }

    @Test
    void createThrowsWhenOutputQuantityNotPositive() {
        UUID processId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        when(processRepository.findById(processId)).thenReturn(Optional.of(emptyProcess(processId)));
        when(operationRepository.findById(operationId))
                .thenReturn(Optional.of(Operation.builder().id(operationId).build()));

        assertThrows(InvalidQuery.class, () -> service.create(processId, ProcessStepRequest.builder()
                .seq(1)
                .operationId(operationId)
                .outputQuantity(0)
                .build()));
    }

    @Test
    void createThrowsWhenSeqAlreadyExistsInProcess() {
        UUID processId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        Process process = emptyProcess(processId);
        process.getSteps().add(ProcessStep.builder().id(UUID.randomUUID()).seq(1).build());

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));
        when(operationRepository.findById(operationId))
                .thenReturn(Optional.of(Operation.builder().id(operationId).build()));

        assertThrows(InvalidQuery.class, () -> service.create(processId, ProcessStepRequest.builder()
                .seq(1)
                .operationId(operationId)
                .outputQuantity(1)
                .build()));
    }

    @Test
    void updateStep() {
        UUID stepId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        Operation operation = Operation.builder().id(operationId).code("TEST").build();
        Process process = emptyProcess(UUID.randomUUID());
        ProcessStep step = ProcessStep.builder().id(stepId).seq(1).process(process).build();
        process.getSteps().add(step);

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(step));
        when(operationRepository.findById(operationId)).thenReturn(Optional.of(operation));
        when(stepRepository.save(any(ProcessStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessStep updated = service.update(ProcessStepRequest.builder()
                .id(stepId)
                .seq(2)
                .operationId(operationId)
                .outputQuantity(3)
                .build());

        assertEquals(2, updated.getSeq());
        assertEquals(operation, updated.getOperation());
        assertEquals(3, updated.getOutputQuantity());
    }

    @Test
    void updateAllowsSameSeqOnSameStep() {
        UUID stepId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        Process process = emptyProcess(UUID.randomUUID());
        ProcessStep step = ProcessStep.builder().id(stepId).seq(1).process(process).build();
        process.getSteps().add(step);

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(step));
        when(operationRepository.findById(operationId))
                .thenReturn(Optional.of(Operation.builder().id(operationId).build()));
        when(stepRepository.save(any(ProcessStep.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessStep updated = service.update(ProcessStepRequest.builder()
                .id(stepId)
                .seq(1)
                .operationId(operationId)
                .outputQuantity(1)
                .build());

        assertEquals(1, updated.getSeq());
    }

    @Test
    void updateThrowsWhenSeqCollidesWithDifferentStep() {
        UUID stepId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        Process process = emptyProcess(UUID.randomUUID());
        ProcessStep step = ProcessStep.builder().id(stepId).seq(1).process(process).build();
        ProcessStep otherStep = ProcessStep.builder().id(UUID.randomUUID()).seq(2).process(process).build();
        process.getSteps().add(step);
        process.getSteps().add(otherStep);

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(step));
        when(operationRepository.findById(operationId))
                .thenReturn(Optional.of(Operation.builder().id(operationId).build()));

        assertThrows(InvalidQuery.class, () -> service.update(ProcessStepRequest.builder()
                .id(stepId)
                .seq(2)
                .operationId(operationId)
                .outputQuantity(1)
                .build()));
    }

    @Test
    void updateThrowsWhenNotFound() {
        UUID stepId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.update(ProcessStepRequest.builder().id(stepId).build()));
    }

    @Test
    void updateThrowsWhenOperationNotFound() {
        UUID stepId = UUID.randomUUID();
        UUID operationId = UUID.randomUUID();
        when(stepRepository.findById(stepId))
                .thenReturn(Optional.of(ProcessStep.builder()
                        .id(stepId)
                        .process(emptyProcess(UUID.randomUUID()))
                        .build()));
        when(operationRepository.findById(operationId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(ProcessStepRequest.builder()
                .id(stepId)
                .operationId(operationId)
                .build()));
    }

    @Test
    void getStep() {
        UUID stepId = UUID.randomUUID();
        ProcessStep step = ProcessStep.builder().id(stepId).build();
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(step));
        assertEquals(step, service.get(stepId));
    }

    @Test
    void getThrowsWhenNotFound() {
        UUID stepId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(stepId));
    }

    @Test
    void deleteStep() {
        UUID stepId = UUID.randomUUID();
        ProcessStep step = ProcessStep.builder().id(stepId).process(emptyProcess(UUID.randomUUID())).build();
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(step));

        ProcessStep deleted = service.delete(stepId);

        InOrder inOrder = Mockito.inOrder(componentService, stepRepository);
        inOrder.verify(componentService).deleteAllForStep(stepId);
        inOrder.verify(stepRepository).delete(step);
        assertEquals(step, deleted);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        UUID stepId = UUID.randomUUID();
        when(stepRepository.findById(stepId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.delete(stepId));
    }

    @Test
    void createThrowsWhenProcessNotDraft() {
        UUID processId = UUID.randomUUID();
        Process process = emptyProcess(processId);
        process.setStatus(ProcessStatus.ACTIVE);
        when(processRepository.findById(processId)).thenReturn(Optional.of(process));

        assertThrows(InvalidQuery.class, () -> service.create(processId, ProcessStepRequest.builder()
                .seq(1)
                .operationId(UUID.randomUUID())
                .outputQuantity(1)
                .build()));
        verify(stepRepository, never()).save(any());
    }

    @Test
    void updateThrowsWhenProcessNotDraft() {
        UUID stepId = UUID.randomUUID();
        Process process = emptyProcess(UUID.randomUUID());
        process.setStatus(ProcessStatus.ARCHIVED);
        ProcessStep step = ProcessStep.builder().id(stepId).seq(1).process(process).build();
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(step));

        assertThrows(InvalidQuery.class, () -> service.update(ProcessStepRequest.builder()
                .id(stepId)
                .seq(1)
                .operationId(UUID.randomUUID())
                .outputQuantity(1)
                .build()));
        verify(stepRepository, never()).save(any());
    }

    @Test
    void deleteThrowsWhenProcessNotDraft() {
        UUID stepId = UUID.randomUUID();
        Process process = emptyProcess(UUID.randomUUID());
        process.setStatus(ProcessStatus.ACTIVE);
        ProcessStep step = ProcessStep.builder().id(stepId).process(process).build();
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(step));

        assertThrows(InvalidQuery.class, () -> service.delete(stepId));
        verify(stepRepository, never()).delete(any());
    }
}
