package com.github.rrin.process.repository;

import com.github.rrin.process.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessStepRepository extends JpaRepository<ProcessStep, UUID> {
}
