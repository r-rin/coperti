package com.github.rrin.process.service.implementation;

import com.github.rrin.exception.ValidationCheck;
import com.github.rrin.exception.types.EntityNotFoundException;
import com.github.rrin.process.Process;
import com.github.rrin.process.dto.ProcessRequest;
import com.github.rrin.process.repository.ProcessRepository;
import com.github.rrin.process.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProcessServiceImpl implements ProcessService {

    private ProcessRepository processRepository;

    @Autowired
    public ProcessServiceImpl(ProcessRepository processRepository) {
        this.processRepository = processRepository;
    }

    @Override
    public Process get(UUID id) {
        Optional<Process> process = processRepository.findById(id);
        new ValidationCheck()
                .check(process.isPresent(), "Process with id " + id + " not found")
                .throwIfAny(EntityNotFoundException::new);

        return process.orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public Process create(ProcessRequest process) {
        return null;
    }

    @Override
    public Process update(ProcessRequest process) {
        return null;
    }

    @Override
    public Process delete(UUID id) {
        return null;
    }

    @Override
    public Process setDrafted(UUID id) {
        return null;
    }

    @Override
    public Process setActive(UUID id) {
        return null;
    }

    @Override
    public Process setArchived(UUID id) {
        return null;
    }
}
