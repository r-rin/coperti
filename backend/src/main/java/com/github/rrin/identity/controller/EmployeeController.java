package com.github.rrin.identity.controller;

import com.github.rrin.identity.Employee;
import com.github.rrin.identity.dto.EmployeeRequest;
import com.github.rrin.identity.dto.EmployeeResponse;
import com.github.rrin.identity.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeResponse> getAll() {
        return employeeService.getAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable UUID id) {
        return toResponse(employeeService.get(id));
    }

    @PostMapping
    public EmployeeResponse create(@RequestBody EmployeeRequest request) {
        return toResponse(employeeService.create(request));
    }

    @PutMapping
    public EmployeeResponse update(@RequestBody EmployeeRequest request) {
        return toResponse(employeeService.update(request));
    }

    @DeleteMapping("/{id}")
    public EmployeeResponse delete(@PathVariable UUID id) {
        return toResponse(employeeService.delete(id));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .build();
    }
}
