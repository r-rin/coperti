package com.github.rrin.identity.service;

import com.github.rrin.identity.Employee;
import com.github.rrin.identity.dto.EmployeeRequest;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {
    Employee create(EmployeeRequest request);
    Employee update(EmployeeRequest request);
    Employee get(UUID id);
    Employee delete(UUID id);

    /** The workforce is small enough to list whole; this backs the employee picker in the UI. */
    List<Employee> getAll();
}
