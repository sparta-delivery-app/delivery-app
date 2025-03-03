package com.example.deliveryapp.domain.order.dto.response;

import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.entity.OrderMenu;
import com.example.deliveryapp.domain.order.enums.OrderState;
import lombok.Getter;

@Getter
public class OrderResponse {
    private final Long id;
    private final Long userId;
    private final Long storeId;
    private final OrderState orderState;
    private final OrderMenuResponse orderMenuResponse;

    public OrderResponse(Order order, OrderMenu orderMenu) {
        this.id = order.getId();
        this.userId = order.getUser().getId();
        this.storeId = order.getStore().getId();
        this.orderState = order.getOrderState();
        this.orderMenuResponse = new OrderMenuResponse(orderMenu);
    }
}
