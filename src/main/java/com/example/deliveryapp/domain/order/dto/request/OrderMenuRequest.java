package com.example.deliveryapp.domain.order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderMenuRequest {
    private Long menuId;
    private String name;
    private Long price;
}
