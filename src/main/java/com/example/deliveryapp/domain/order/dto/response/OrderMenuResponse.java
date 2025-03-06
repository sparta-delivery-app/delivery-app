package com.example.deliveryapp.domain.order.dto.response;

import com.example.deliveryapp.domain.order.entity.OrderMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderMenuResponse {
    private Long menuId;
    private String name;
    private Long price;

    public OrderMenuResponse(OrderMenu orderMenu) {
        this.menuId = orderMenu.getMenuId();
        this.name = orderMenu.getName();
        this.price = orderMenu.getPrice();
    }
}
