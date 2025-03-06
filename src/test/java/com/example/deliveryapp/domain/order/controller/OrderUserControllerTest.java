package com.example.deliveryapp.domain.order.controller;

import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.order.converter.OrderConverter;
import com.example.deliveryapp.domain.order.dto.request.CartAddRequest;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.entity.OrderMenu;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.order.service.CartService;
import com.example.deliveryapp.domain.order.service.OrderService;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.enums.StoreStatus;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderUserController.class)
public class OrderUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CartService cartService;

    @Test
    void 주문_생성_성공() throws Exception {
        Long userId = 1L;
        User user = new User("em@em.com", "pw", "name", UserRole.USER);
        Store store = new Store(
                "name", LocalTime.of(9, 0), LocalTime.of(22, 0),
                1000L, StoreStatus.OPEN, user);

        String bearerToken = "bearerToken";

        Order order = new Order(user, store, OrderState.CART);
        OrderMenu orderMenu = new OrderMenu(new Menu("name1", 1000L, "description", store));
        ReflectionTestUtils.setField(orderMenu, "id", 1L);
        OrderMenu orderMenu1 = new OrderMenu(new Menu("name2", 200L, "description", store));
        ReflectionTestUtils.setField(orderMenu1, "id", 2L);
        order.addOrderMenu(orderMenu);
        order.addOrderMenu(orderMenu1);

        OrderResponse orderResponse = OrderConverter.toResponse(order);

        given(orderService.createOrder(anyLong())).willReturn(orderResponse);

        mockMvc.perform(post("/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
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
    void 장바구니_추가_성공() throws Exception {
        long userId = 1L;
        long menuId = 1L;

        String bearerToken = "bearerToken";

        CartAddRequest cartAddRequest = new CartAddRequest(menuId, null);
        doNothing().when(cartService).addCart(userId,cartAddRequest);

        mockMvc.perform(post("/carts",menuId)
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
    void 사용자의_주문_목록_조회_성공() throws Exception {
        Long userId = 1L;
        User user = new User("em@em.com", "pw", "name", UserRole.USER);
        Store store = new Store(
                "name", LocalTime.of(9, 0), LocalTime.of(22, 0),
                1000L, StoreStatus.OPEN, user);

        String bearerToken = "bearerToken";

        Order order = new Order(user, store, OrderState.PENDING);

        OrderMenu orderMenu = new OrderMenu(new Menu("menu1",10000L, "description", store));
        OrderMenu orderMenu2 = new OrderMenu(new Menu("menu2",10000L, "description", store));
        order.addOrderMenu(orderMenu);
        order.addOrderMenu(orderMenu2);
        OrderResponse response = OrderConverter.toResponse(order);

        List<OrderResponse> orderList = List.of(response);

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
                .andExpect(jsonPath("$.length()").value(1));
    }
}
