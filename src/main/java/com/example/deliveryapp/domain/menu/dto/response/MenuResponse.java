package com.example.deliveryapp.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuResponse {
    private final Long menuId;
    private final String menuName;
    private final Long price;
}
