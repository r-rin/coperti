package com.github.rrin.inventory.repository;

import com.github.rrin.inventory.OtherUse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface OtherUseRepository extends JpaRepository<OtherUse, UUID>, JpaSpecificationExecutor<OtherUse> {
}
