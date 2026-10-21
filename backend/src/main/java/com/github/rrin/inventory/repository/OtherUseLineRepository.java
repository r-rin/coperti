package com.github.rrin.inventory.repository;

import com.github.rrin.inventory.OtherUseLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OtherUseLineRepository extends JpaRepository<OtherUseLine, UUID> {
    List<OtherUseLine> findAllByOtherUse_Id(UUID otherUseId);
}
