package com.github.rrin.inventory.controller;

import com.github.rrin.inventory.dto.OtherUseDecisionRequest;
import com.github.rrin.inventory.dto.OtherUseRequest;
import com.github.rrin.inventory.dto.OtherUseResponse;
import com.github.rrin.inventory.dto.filter.OtherUseFilter;
import com.github.rrin.inventory.service.OtherUseService;
import com.github.rrin.utils.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/other-use")
public class OtherUseController {

    private final OtherUseService otherUseService;
    private final InventoryMapper mapper;

    @Autowired
    public OtherUseController(OtherUseService otherUseService, InventoryMapper mapper) {
        this.otherUseService = otherUseService;
        this.mapper = mapper;
    }

    @GetMapping
    public PaginatedResponse<OtherUseResponse> search(@ModelAttribute OtherUseFilter filter,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return PaginatedResponse.of(otherUseService.search(filter, page, size).map(mapper::toResponse));
    }

    @GetMapping("/{id}")
    public OtherUseResponse get(@PathVariable UUID id) {
        return mapper.toResponse(otherUseService.get(id));
    }

    @PostMapping
    public OtherUseResponse create(@RequestBody OtherUseRequest request) {
        return mapper.toResponse(otherUseService.create(request));
    }

    @PostMapping("/{id}/approve")
    public OtherUseResponse approve(@PathVariable UUID id, @RequestBody OtherUseDecisionRequest request) {
        return mapper.toResponse(otherUseService.approve(id, request));
    }

    @PostMapping("/{id}/reject")
    public OtherUseResponse reject(@PathVariable UUID id, @RequestBody OtherUseDecisionRequest request) {
        return mapper.toResponse(otherUseService.reject(id, request));
    }
}
