package com.github.rrin.production.controller;

import com.github.rrin.production.dto.StepAssignmentResponse;
import com.github.rrin.production.service.StepHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/step-assignment")
public class StepAssignmentController {

    private final StepHistoryService historyService;
    private final ProductionMapper mapper;

    @Autowired
    public StepAssignmentController(StepHistoryService historyService, ProductionMapper mapper) {
        this.historyService = historyService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<StepAssignmentResponse> forEmployee(@RequestParam UUID employeeId,
                                                    @RequestParam(required = false) Instant from,
                                                    @RequestParam(required = false) Instant to) {
        return historyService.getAssignmentsForEmployee(employeeId, from, to).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
