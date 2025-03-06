package com.example.deliveryapp.domain.order.converter;

import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse.OrderMenuResponse;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse.OrderMenuResponse.OrderMenuOptionResponse;
import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.entity.OrderMenu;
import com.example.deliveryapp.domain.order.entity.OrderMenuOption;

import java.util.List;

public class OrderConverter {
    public static OrderResponse toResponse(Order order) {
        List<OrderMenuResponse> orderMenuResponses = order.getOrderMenus().stream()
                .map(OrderConverter::toResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStore().getId(),
                order.getOrderState(),
                order.calculateTotalPrice(),
                orderMenuResponses
        );
    }

    public static OrderMenuResponse toResponse(OrderMenu orderMenu) {
        List<OrderMenuOptionResponse> orderMenuOptionResponses = orderMenu.getOrderMenuOptions().stream()
                .map(OrderConverter::toResponse)
                .toList();

        return new OrderMenuResponse(
                orderMenu.getId(),
                orderMenu.getMenu().getId(),
                orderMenu.getName(),
                orderMenu.getPrice(),
                orderMenuOptionResponses
        );
    }

    public static OrderMenuOptionResponse toResponse(OrderMenuOption orderMenuOption) {
        return new OrderMenuOptionResponse(
                orderMenuOption.getId(),
                orderMenuOption.getOptionItem().getId(),
                orderMenuOption.getName(),
                orderMenuOption.getAdditionalPrice()
        );
    }
}
