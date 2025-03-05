package com.example.deliveryapp.domain.order.controller;

import com.example.deliveryapp.domain.common.annotation.Auth;
import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.order.dto.request.OrderStateUpdateRequest;
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
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    // 전체 주문
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @Auth AuthUser authUser) {
        return ResponseEntity.ok(orderService.createOrder(authUser.getId()));
    }

    // 장바구니 추가
    @PostMapping("/menus/{menuId}/orders")
    public ResponseEntity<Void> addCart(
            @Auth AuthUser authUser, @PathVariable Long menuId) {
        cartService.addCart(authUser.getId(), menuId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/orders")
    public List<OrderResponse> getUserOrders(@Auth AuthUser authUser) {
        return orderService.getOrdersByUserId(authUser.getId());
    }

    @GetMapping("/stores/{storeId}/orders")
    public List<OrderResponse> getStoreOrders(@PathVariable Long storeId, @Auth AuthUser authUser) {
        return orderService.getOrdersByStoreId(storeId, authUser.getId());
    }

    @PatchMapping("/orders/{orderId}")
    public ResponseEntity<Void> updateOrderState(
            @Auth AuthUser authUser,
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStateUpdateRequest request) {
        orderService.updateOrderState(authUser.getId(), orderId, request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
