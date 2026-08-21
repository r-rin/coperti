package com.github.rrin.process.repository;

import com.github.rrin.process.Process;
import com.github.rrin.process.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProcessRepository extends JpaRepository<Process, UUID> {
    Optional<Process> findProcessByStatus(ProcessStatus status);
}
