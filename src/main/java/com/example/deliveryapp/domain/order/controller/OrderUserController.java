package com.example.deliveryapp.domain.order.controller;

import com.example.deliveryapp.domain.common.annotation.Auth;
import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderUserController {

    private final OrderService orderService;

    // 전체 주문
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Auth AuthUser authUser) {
        return ResponseEntity.ok(orderService.createOrder(authUser.getId()));
    }

    @GetMapping("/orders")
    public List<OrderResponse> getUserOrders(@Auth AuthUser authUser) {
        return orderService.getOrdersByUserId(authUser.getId());
    }
}
