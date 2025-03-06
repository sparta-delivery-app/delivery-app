package com.example.deliveryapp.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MenuResponse {
    private final Long menuId;
    private final String menuName;
    private final Long price;
    private final String description;
}
