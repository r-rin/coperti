package com.github.rrin.process.service.implementation;

import com.github.rrin.exception.ValidationCheck;
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
import com.github.rrin.process.service.ProcessService;
import com.github.rrin.process.service.ProcessStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class ProcessServiceImpl implements ProcessService {

    private final ProcessRepository processRepository;
    private final ItemRepository itemRepository;
    private final ProcessStepService processStepService;

    @Autowired
    public ProcessServiceImpl(ProcessRepository processRepository,
                              ItemRepository itemRepository,
                              ProcessStepService processStepService) {
        this.processRepository = processRepository;
        this.itemRepository = itemRepository;
        this.processStepService = processStepService;
    }

    @Override
    public Process get(UUID id) {
        return checkIfProcessExists(id);
    }

    @Override
    public Process create(ProcessRequest request) {
        Item producedItem = resolveProducedItem(request.getProducedItemId());
        validateVersion(request.getVersion());

        Process process = new Process();
        process.setProduces(producedItem);
        process.setVersion(request.getVersion());
        process.setStatus(request.getStatus() == null ? ProcessStatus.DRAFT : request.getStatus());
        process.setSteps(Collections.emptyList());
        Process saved = processRepository.save(process);

        if (request.getSteps() != null) {
            for (ProcessStepRequest stepRequest : request.getSteps()) {
                processStepService.create(saved.getId(), stepRequest);
            }
        }
        return get(saved.getId());
    }

    @Override
    public Process update(ProcessRequest request) {
        Process process = checkIfProcessExists(request.getId());
        Item producedItem = resolveProducedItem(request.getProducedItemId());
        validateVersion(request.getVersion());

        // status has dedicated setters; steps are managed via ProcessStepService
        process.setProduces(producedItem);
        process.setVersion(request.getVersion());
        return processRepository.save(process);
    }

    @Override
    public Process delete(UUID id) {
        Process process = checkIfProcessExists(id);
        for (ProcessStep step : process.getSteps()) {
            processStepService.delete(step.getId());
        }
        processRepository.delete(process);
        return process;
    }

    @Override
    public Process setDrafted(UUID id) {
        return setStatus(id, ProcessStatus.DRAFT);
    }

    @Override
    public Process setActive(UUID id) {
        return setStatus(id, ProcessStatus.ACTIVE);
    }

    @Override
    public Process setArchived(UUID id) {
        return setStatus(id, ProcessStatus.ARCHIVED);
    }

    private Process setStatus(UUID id, ProcessStatus status) {
        Process process = checkIfProcessExists(id);
        process.setStatus(status);
        return processRepository.save(process);
    }

    private Item resolveProducedItem(UUID producedItemId) {
        new ValidationCheck()
                .check(producedItemId != null, "Produced item id is required")
                .throwIfAny(InvalidQuery::new);
        return itemRepository.findById(producedItemId)
                .orElseThrow(() -> new EntityNotFoundException("Produced item not found with id: " + producedItemId));
    }

    private void validateVersion(int version) {
        new ValidationCheck()
                .check(version > 0, "Version must be greater than 0")
                .throwIfAny(InvalidQuery::new);
    }

    private Process checkIfProcessExists(UUID id) {
        return processRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Process not found with id: " + id));
    }
}
