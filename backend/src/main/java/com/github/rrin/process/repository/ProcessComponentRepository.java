package com.github.rrin.process.repository;

import com.github.rrin.process.ProcessComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessComponentRepository extends JpaRepository<ProcessComponent, UUID> {
    List<ProcessComponent> findAllByProcessStep_Id(UUID processStepId);
}
