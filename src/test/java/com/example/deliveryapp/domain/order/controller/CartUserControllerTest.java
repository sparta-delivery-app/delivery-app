package com.example.deliveryapp.domain.order.controller;

import com.example.deliveryapp.domain.order.dto.request.CartAddRequest;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.service.CartService;
import com.example.deliveryapp.domain.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartUserController.class)
public class CartUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CartService cartService;

    @Test
    void 장바구니_추가_성공() throws Exception {
        long userId = 1L;
        long menuId = 1L;

        String bearerToken = "bearerToken";

        CartAddRequest cartAddRequest = new CartAddRequest(menuId, null);
        doNothing().when(cartService).addCart(userId, cartAddRequest);

        mockMvc.perform(post("/carts", menuId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartAddRequest))
                        .with(request -> {
                            request.setAttribute("userId", userId);
                            request.setAttribute("email", "em@em.com");
                            request.setAttribute("name", "name");
                            request.setAttribute("userRole", "USER");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

    @Test
    void 장바구니_조회_성공() throws Exception {
        long userId = 1L;

        OrderResponse.OrderMenuResponse orderMenuResponse = new OrderResponse.OrderMenuResponse(
                1L, 1L, "Menu Name", 1000L, List.of(
                new OrderResponse.OrderMenuResponse.OrderMenuOptionResponse(
                        1L, 1L, "Option Name", 100L)));
        List<OrderResponse.OrderMenuResponse> cartItems = List.of(orderMenuResponse);

        when(cartService.getCartItems(userId)).thenReturn(cartItems);

        String bearerToken = "bearerToken";

        mockMvc.perform(get("/carts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                        .with(request -> {
                            request.setAttribute("userId", userId);
                            request.setAttribute("email", "em@em.com");
                            request.setAttribute("name", "name");
                            request.setAttribute("userRole", "USER");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderMenuId").value(1L))
                .andExpect(jsonPath("$[0].menuId").value(1L))
                .andExpect(jsonPath("$[0].menuName").value("Menu Name"))
                .andExpect(jsonPath("$[0].price").value(1000L))
                .andExpect(jsonPath("$[0].orderMenuOptions[0].optionItemId").value(1L))
                .andExpect(jsonPath("$[0].orderMenuOptions[0].optionItemName").value("Option Name"))
                .andExpect(jsonPath("$[0].orderMenuOptions[0].additionalPrice").value(100L));
    }

    @Test
    void 장바구니_메뉴_삭제() throws Exception {
        long userId = 1L;
        long orderMenuId = 1L;

        doNothing().when(cartService).removeCartItem(userId, orderMenuId);

        String bearerToken = "bearerToken";

        mockMvc.perform(delete("/orders/{orderMenuId}", orderMenuId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                        .with(request -> {
                            request.setAttribute("userId", userId);
                            request.setAttribute("email", "em@em.com");
                            request.setAttribute("name", "name");
                            request.setAttribute("userRole", "USER");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

}
