package com.example.deliveryapp.domain.order.controller;

import com.example.deliveryapp.domain.common.annotation.Auth;
import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.order.dto.request.CartAddRequest;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.service.CartService;
import com.example.deliveryapp.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderUserController {

    private final OrderService orderService;
    private final CartService cartService;

    // 전체 주문
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Auth AuthUser authUser) {
        return ResponseEntity.ok(orderService.createOrder(authUser.getId()));
    }

    @PostMapping("/carts")
    public ResponseEntity<Void> addCart(
            @Auth AuthUser authUser,
            @RequestBody @Valid CartAddRequest cartAddRequest
    ) {
        cartService.addCart(authUser.getId(), cartAddRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/orders")
    public List<OrderResponse> getUserOrders(@Auth AuthUser authUser) {
        return orderService.getOrdersByUserId(authUser.getId());
    }
}
