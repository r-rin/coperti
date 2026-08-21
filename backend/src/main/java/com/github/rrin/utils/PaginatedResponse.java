package com.github.rrin.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class PaginatedResponse<T> {
    private List<T> data;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
    private boolean hasNextPage;
    private boolean hasPreviousPage;

    public static <T> PaginatedResponse<T> of(Page<T> page) {
        PaginatedResponse<T> response = new PaginatedResponse<>();
        response.setData(page.getContent());
        response.setCurrentPage(page.getNumber());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems((int) page.getTotalElements());
        response.setPageSize(page.getSize());
        response.setHasNextPage(page.hasNext());
        response.setHasPreviousPage(page.hasPrevious());
        return response;
    }
}
