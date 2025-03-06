package com.example.deliveryapp.domain.order.controller;

import com.example.deliveryapp.domain.common.annotation.Auth;
import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.order.dto.request.CartAddRequest;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CartUserController {

    private final CartService cartService;

    //장바구니 추가
    @PostMapping("/carts")
    public ResponseEntity<Void> addCart(
            @Auth AuthUser authUser,
            @RequestBody @Valid CartAddRequest cartAddRequest
    ) {
        cartService.addCart(authUser.getId(), cartAddRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //장바구니 조회
    @GetMapping("/carts")
    public List<OrderResponse.OrderMenuResponse> getCart(@Auth AuthUser authUser) {
        return cartService.getCartItems(authUser.getId());
    }

    @DeleteMapping("/orders/{orderMenuId}")
    public ResponseEntity<Void> removeCartItem(
            @Auth AuthUser authUser,
            @PathVariable Long orderMenuId
    ) {
        cartService.removeCartItem(authUser.getId(), orderMenuId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
