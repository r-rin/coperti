package com.github.rrin.process.repository;

import com.github.rrin.process.Operation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperationRepository extends JpaRepository<Operation, UUID> {

}
