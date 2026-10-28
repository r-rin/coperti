package com.github.rrin;

import com.github.rrin.exception.types.ConflictException;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.dto.EmployeeRequest;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.identity.service.implementation.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl service;

    private final Employee ann = Employee.builder().id(UUID.randomUUID()).name("Ann").build();

    @Test
    void createTrimsName() {
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals("Ann", service.create(EmployeeRequest.builder().name("  Ann ").build()).getName());
    }

    @Test
    void createRequiresName() {
        assertThrows(ValidationException.class, () -> service.create(EmployeeRequest.builder().name(" ").build()));
    }

    @Test
    void deleteWithoutHistory() {
        when(employeeRepository.findById(ann.getId())).thenReturn(Optional.of(ann));
        assertEquals(ann, service.delete(ann.getId()));
        verify(employeeRepository).delete(ann);
    }

    @Test
    void deleteWithHistoryIsAConflictNotAServerError() {
        when(employeeRepository.findById(ann.getId())).thenReturn(Optional.of(ann));
        doThrow(new DataIntegrityViolationException("FK")).when(employeeRepository).delete(ann);

        ConflictException error = assertThrows(ConflictException.class, () -> service.delete(ann.getId()));
        assertEquals(true, error.getMessages().get(0).contains("Ann"));
    }

    @Test
    void deleteMissingEmployee() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.delete(id));
    }
}
