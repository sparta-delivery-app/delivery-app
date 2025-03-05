package com.example.deliveryapp.domain.order.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.entity.OrderMenu;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.enums.StoreStatus;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private CartService cartService;

    @Nested
    class 장바구니_추가 {
        @Test
        void user_조회_실패() {
            long userId = 1L;
            long storeId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);

            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThrows(CustomException.class,
                    () -> cartService.addCart(userId, storeId), "사용자를 찾을 수 없습니다");
        }

        @Test
        void menu_조회_실패() {
            long userId = 1L;
            long storeId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);
            Store store = new Store(
                    "name", LocalTime.of(0, 0), LocalTime.of(23, 59),
                    1000L, StoreStatus.OPEN, user);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(menuRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThrows(CustomException.class,
                    () -> cartService.addCart(userId, storeId), "메뉴를 찾을 수 없습니다");
        }

        @Test
        void order_조회_실패() {
            long userId = 1L;
            long storeId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);
            Store store = new Store(
                    "name", LocalTime.of(0, 0), LocalTime.of(23, 59),
                    1000L, StoreStatus.OPEN,user);
            Menu menu = new Menu("name",1000L, "description", store);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(menuRepository.findById(anyLong())).willReturn(Optional.of(menu));
            given(orderRepository.findByUserIdAndOrderState(anyLong(),any(OrderState.class)))
                    .willThrow(new CustomException(ErrorCode.ORDER_NOT_FOUND));

            assertThrows(CustomException.class,
                    () -> cartService.addCart(userId, storeId), "주문을 찾을 수 없습니다");
        }

        @Test
        void 장바구니_가게가_다른_경우_초기화_후_추가_성공() {
            long userId = 1L;
            long storeId = 1L;
            long menuId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);
            Store store = new Store(
                    "name", LocalTime.of(0, 0), LocalTime.of(23, 59),
                    1000L, StoreStatus.OPEN, user);
            ReflectionTestUtils.setField(store,"id",storeId);

            Store store2 = new Store(
                    "name", LocalTime.of(0, 0), LocalTime.of(23, 59),
                    1000L, StoreStatus.OPEN, user);
            ReflectionTestUtils.setField(store,"id",2L);

            Menu menu = new Menu("name",1000L, "description", store);

            Order order = new Order(user, store2, OrderState.CART);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(menuRepository.findById(anyLong())).willReturn(Optional.of(menu));
            given(orderRepository.findByUserIdAndOrderState(anyLong(),any(OrderState.class)))
                    .willReturn(Optional.of(order));

            cartService.addCart(userId,menuId);

            verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        void 장바구니_없는_상태_장바구니_추가_성공() {
            long userId = 1L;
            long storeId = 1L;
            long menuId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);
            Store store = new Store(
                    "name", LocalTime.of(0, 0), LocalTime.of(23, 59),
                    1000L, StoreStatus.OPEN, user);
            Menu menu = new Menu("name",1000L, "description", store);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(menuRepository.findById(anyLong())).willReturn(Optional.of(menu));
            given(orderRepository.findByUserIdAndOrderState(anyLong(),any(OrderState.class)))
                    .willReturn(Optional.empty());

            cartService.addCart(userId,menuId);

            verify(orderRepository, times(1)).save(any(Order.class));
        }
    }

    @Test
    void testCleanupCartsWhenOrdersExist() {
        // Given
        Long userId1 = 1L;
        User user1 = new User("em@em.com", "pw", "name", UserRole.USER);
        Store store1 = new Store("name", LocalTime.of(9, 0), LocalTime.of(22, 0),
                1000L, StoreStatus.OPEN, user1);
        Order order1 = new Order(user1, store1, OrderState.CART);
        order1.setId(1L);
        order1.setUpdatedAt(LocalDateTime.now().minusHours(25));

        Long userId2 = 2L;
        User user2 = new User("em2@em.com", "pw", "name", UserRole.USER);
        Store store2 = new Store("name2", LocalTime.of(9, 0), LocalTime.of(22, 0),
                1000L, StoreStatus.OPEN, user2);
        Order order2 = new Order(user2, store2, OrderState.CART);
        order2.setId(2L);
        order2.setUpdatedAt(LocalDateTime.now().minusHours(5));
        OrderMenu orderMenu = new OrderMenu(order1,1L,"name",1000L);
        order1.addOrderMenu(orderMenu);
        order2.addOrderMenu(orderMenu);
        List<Order> orders = Arrays.asList(order1, order2);

        when(orderRepository.findAll()).thenReturn(orders);

        given(orderRepository.findByUserIdAndOrderState(userId1, OrderState.CART)).willReturn(Optional.of(order1));
        given(orderRepository.findByUserIdAndOrderState(userId2, OrderState.CART)).willReturn(Optional.of(order2));

        // When
        cartService.cleanupCarts();

        // Then
        verify(orderRepository, times(1)).findAll();
        verify(orderRepository, times(1)).save(order1);
        verify(orderRepository, times(0)).save(order2);
        assertTrue(order1.getOrderMenus().isEmpty());
        assertFalse(order2.getOrderMenus().isEmpty());
    }

}
