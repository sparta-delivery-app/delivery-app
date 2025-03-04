package com.example.deliveryapp.domain.order.controller;

import com.example.deliveryapp.domain.order.dto.request.OrderMenuRequest;
import com.example.deliveryapp.domain.order.dto.request.OrderRequest;
import com.example.deliveryapp.domain.order.dto.request.OrderStateUpdateRequest;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.entity.OrderMenu;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.order.service.OrderService;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void 주문_생성_성공() throws Exception {
        Long userId = 1L;
        User user = new User("em@em.com", "pw", "name", UserRole.USER);
        Store store = new Store(
                "name", LocalTime.of(9, 0), LocalTime.of(22, 0), 1000L, user);

        String bearerToken = "bearerToken";

        OrderRequest orderRequest = new OrderRequest(
                1L, new OrderMenuRequest(1L, "name", 10000L));

        Order order = new Order(user, store, OrderState.PENDING);
        OrderMenu orderMenu = new OrderMenu(order, 1L, "menu1", 10000L);

        OrderResponse orderResponse = new OrderResponse(order, orderMenu);

        given(orderService.createOrder(anyLong(), any(OrderRequest.class))).willReturn(orderResponse);

        mockMvc.perform(post("/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest))
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
    void 사용자의_주문_목록_조회_성공() throws Exception {
        Long userId = 1L;
        User user = new User("em@em.com", "pw", "name", UserRole.USER);
        Store store = new Store(
                "name", LocalTime.of(9, 0), LocalTime.of(22, 0), 1000L, user);

        String bearerToken = "bearerToken";

        Order order = new Order(user, store, OrderState.PENDING);

        OrderMenu orderMenu = new OrderMenu(order, 1L, "menu1", 10000L);
        OrderMenu orderMenu2 = new OrderMenu(order, 2L, "menu2", 10000L);

        List<OrderResponse> orderList = List.of(
                new OrderResponse(order, orderMenu), new OrderResponse(order, orderMenu2));

        given(orderService.getOrdersByUserId(anyLong())).willReturn(orderList);

        mockMvc.perform(get("/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("userId", userId);
                            request.setAttribute("email", "em@em.com");
                            request.setAttribute("name", "name");
                            request.setAttribute("userRole", "USER");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void 사장님의_가게별_주문_목록_조회_성공() throws Exception {
        Long userId = 1L;
        User user1 = new User("em@em.com", "pw", "name", UserRole.OWNER);
        User user2 = new User("em2@em.com", "pw", "name2", UserRole.USER);
        User user3 = new User("em3@em.com", "pw", "name3", UserRole.USER);

        Long storeId = 1L;
        Store store = new Store(
                "name", LocalTime.of(9, 0), LocalTime.of(22, 0), 1000L, user1);
        ReflectionTestUtils.setField(store, "id", storeId);

        String bearerToken = "bearerToken";

        Order order = new Order(user1, store, OrderState.PENDING);
        Order order2 = new Order(user2, store, OrderState.PENDING);
        Order order3 = new Order(user3, store, OrderState.PENDING);

        OrderMenu orderMenu = new OrderMenu(order, 1L, "menu1", 10000L);

        List<OrderResponse> orderList = List.of(
                new OrderResponse(order, orderMenu),
                new OrderResponse(order2, orderMenu),
                new OrderResponse(order3, orderMenu));

        given(orderService.getOrdersByStoreId(anyLong(), anyLong())).willReturn(orderList);

        mockMvc.perform(get("/stores/{storeId}/orders", storeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("userId", userId);
                            request.setAttribute("email", "em@em.com");
                            request.setAttribute("name", "name");
                            request.setAttribute("userRole", "USER");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void 주문_상태_변경_성공() throws Exception {
        Long userId = 1L;
        Long orderId = 1L;
        String bearerToken = "bearerToken";

        OrderStateUpdateRequest stateUpdateRequest = new OrderStateUpdateRequest(OrderState.ACCEPTED);

        doNothing().when(orderService).updateOrderState(anyLong(),anyLong(),any(OrderStateUpdateRequest.class));

        mockMvc.perform(patch("/orders/{orderId}", orderId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stateUpdateRequest))
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
