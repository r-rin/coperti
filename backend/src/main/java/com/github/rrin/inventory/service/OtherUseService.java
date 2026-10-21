package com.github.rrin.inventory.service;

import com.github.rrin.inventory.OtherUse;
import com.github.rrin.inventory.OtherUseLine;
import com.github.rrin.inventory.dto.OtherUseDecisionRequest;
import com.github.rrin.inventory.dto.OtherUseRequest;
import com.github.rrin.inventory.dto.filter.OtherUseFilter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface OtherUseService {
    OtherUse get(UUID id);
    List<OtherUseLine> getLines(UUID otherUseId);
    Page<OtherUse> search(OtherUseFilter filter, int page, int size);

    /** Records the request only; stock moves on approval. */
    OtherUse create(OtherUseRequest request);
    OtherUse approve(UUID id, OtherUseDecisionRequest request);
    OtherUse reject(UUID id, OtherUseDecisionRequest request);
}
