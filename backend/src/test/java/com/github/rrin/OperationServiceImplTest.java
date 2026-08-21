package com.github.rrin;

import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.process.Operation;
import com.github.rrin.process.dto.OperationRequest;
import com.github.rrin.process.repository.OperationRepository;
import com.github.rrin.process.service.implementation.OperationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationServiceImplTest {

    @Mock
    private OperationRepository repository;

    @InjectMocks
    private OperationServiceImpl service;

    @Test
    void createOperation() {
        OperationRequest request = OperationRequest.builder()
                .code("WELD")
                .name("Weld bracket")
                .description("Welds the bracket to the frame")
                .build();

        when(repository.save(any(Operation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Operation created = service.create(request);

        assertEquals("WELD", created.getCode());
        assertEquals("Weld bracket", created.getName());
        assertEquals("Welds the bracket to the frame", created.getDescription());
    }

    @Test
    void updateOperation() {
        UUID id = UUID.randomUUID();
        Operation operation = Operation.builder().id(id).code("WELD").name("Weld").build();
        when(repository.findById(id)).thenReturn(Optional.of(operation));
        when(repository.save(any(Operation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Operation updated = service.update(OperationRequest.builder()
                .id(id)
                .code("WELD2")
                .name("Weld v2")
                .description("Updated")
                .build());

        assertEquals("WELD2", updated.getCode());
        assertEquals("Weld v2", updated.getName());
        assertEquals("Updated", updated.getDescription());
    }

    @Test
    void updateThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> service.update(OperationRequest.builder().id(id).build()));
    }

    @Test
    void getOperation() {
        UUID id = UUID.randomUUID();
        Operation operation = Operation.builder().id(id).code("TEST").build();
        when(repository.findById(id)).thenReturn(Optional.of(operation));
        assertEquals(operation, service.get(id));
    }

    @Test
    void getThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(id));
    }

    @Test
    void deleteOperation() {
        UUID id = UUID.randomUUID();
        Operation operation = Operation.builder().id(id).code("PACK").build();
        when(repository.findById(id)).thenReturn(Optional.of(operation));

        Operation deleted = service.delete(id);

        verify(repository).delete(operation);
        assertEquals(operation, deleted);
    }

    @Test
    void deleteThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.delete(id));
    }
}
