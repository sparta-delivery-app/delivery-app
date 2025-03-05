package com.example.deliveryapp.domain.menu.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class MenuPageResponse {
    private final List<MenuResponse> content;
    private final Integer currentPage;
    private final Integer totalPages;
    private final Long totalElements;
    private final Integer size;

    public MenuPageResponse(Page<MenuResponse> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber() + 1;
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.size = page.getSize();
    }
}
