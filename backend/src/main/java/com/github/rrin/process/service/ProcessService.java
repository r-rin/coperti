package com.github.rrin.process.service;

import com.github.rrin.process.Process;
import com.github.rrin.process.dto.ProcessRequest;

import java.util.UUID;

public interface ProcessService {
    Process get(UUID id);
    Process create(ProcessRequest process);
    Process update(ProcessRequest process);
    Process delete(UUID id);

    Process setDrafted(UUID id);
    Process setActive(UUID id);
    Process setArchived(UUID id);
}
