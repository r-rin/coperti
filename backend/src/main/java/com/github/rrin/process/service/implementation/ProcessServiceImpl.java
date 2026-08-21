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
        validateVersionIsFree(producedItem.getId(), request.getVersion(), null);

        Process process = new Process();
        process.setProduces(producedItem);
        process.setVersion(request.getVersion());
        // a process always starts as a draft; it only becomes ACTIVE through setActive
        process.setStatus(ProcessStatus.DRAFT);
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
        ProcessGuard.requireDraft(process, "update process");
        Item producedItem = resolveProducedItem(request.getProducedItemId());
        validateVersion(request.getVersion());
        validateVersionIsFree(producedItem.getId(), request.getVersion(), process.getId());

        // status has dedicated setters; steps are managed via ProcessStepService
        process.setProduces(producedItem);
        process.setVersion(request.getVersion());
        return processRepository.save(process);
    }

    @Override
    public Process delete(UUID id) {
        Process process = checkIfProcessExists(id);
        ProcessGuard.requireDraft(process, "delete process");
        for (ProcessStep step : process.getSteps()) {
            processStepService.delete(step.getId());
        }
        processRepository.delete(process);
        return process;
    }

    @Override
    public Process setDrafted(UUID id) {
        Process process = checkIfProcessExists(id);
        new ValidationCheck()
                .check(process.getStatus() != ProcessStatus.ACTIVE,
                        "Active process cannot be moved back to draft, archive it first")
                .throwIfAny(InvalidQuery::new);
        return setStatus(process, ProcessStatus.DRAFT);
    }

    @Override
    public Process setActive(UUID id) {
        Process process = checkIfProcessExists(id);
        if (process.getStatus() == ProcessStatus.ACTIVE) {
            return process;
        }
        new ValidationCheck()
                .check(process.getSteps() != null && !process.getSteps().isEmpty(),
                        "Process must have at least one step to be activated")
                .throwIfAny(InvalidQuery::new);
        // at most one active process per produced item: archive the current one
        processRepository.findFirstByProduces_IdAndStatus(process.getProduces().getId(), ProcessStatus.ACTIVE)
                .ifPresent(current -> setStatus(current, ProcessStatus.ARCHIVED));
        return setStatus(process, ProcessStatus.ACTIVE);
    }

    @Override
    public Process setArchived(UUID id) {
        return setStatus(checkIfProcessExists(id), ProcessStatus.ARCHIVED);
    }

    private Process setStatus(Process process, ProcessStatus status) {
        process.setStatus(status);
        return processRepository.save(process);
    }

    private void validateVersionIsFree(UUID producedItemId, int version, UUID selfId) {
        processRepository.findByProduces_IdAndVersion(producedItemId, version)
                .filter(existing -> !existing.getId().equals(selfId))
                .ifPresent(existing -> {
                    throw new InvalidQuery("Version " + version + " already exists for produced item: " + producedItemId);
                });
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
