package com.github.rrin.identity.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.exception.types.ValidationException;
import com.github.rrin.identity.Employee;
import com.github.rrin.identity.dto.EmployeeRequest;
import com.github.rrin.identity.repository.EmployeeRepository;
import com.github.rrin.identity.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee create(EmployeeRequest request) {
        requireName(request);
        return employeeRepository.save(Employee.builder().name(request.getName().trim()).build());
    }

    @Override
    public Employee update(EmployeeRequest request) {
        Employee employee = getIfExists(request.getId());
        requireName(request);
        employee.setName(request.getName().trim());
        return employeeRepository.save(employee);
    }

    @Override
    public Employee get(UUID id) {
        return getIfExists(id);
    }

    @Override
    public Employee delete(UUID id) {
        Employee employee = getIfExists(id);
        employeeRepository.delete(employee);
        return employee;
    }

    @Override
    public List<Employee> getAll() {
        return employeeRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    private void requireName(EmployeeRequest request) {
        new ValidationCheck()
                .check(request.getName() != null && !request.getName().isBlank(), "Name is required")
                .throwIfAny(ValidationException::new);
    }

    private Employee getIfExists(UUID id) {
        return employeeRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Employee with id " + id + " not found")
        );
    }
}
