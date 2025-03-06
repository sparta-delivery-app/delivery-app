package com.example.deliveryapp.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OptionCategoryPageResponse {
    private final List<OptionCategoryResponse> content;
    private final Integer currentPage;
    private final Integer totalPages;
    private final Long totalElements;
    private final Integer size;
}
