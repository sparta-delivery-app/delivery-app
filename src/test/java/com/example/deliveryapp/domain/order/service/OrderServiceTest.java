package com.example.deliveryapp.domain.order.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.order.dto.request.OrderMenuRequest;
import com.example.deliveryapp.domain.order.dto.request.OrderStateUpdateRequest;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.entity.OrderMenu;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.order.repository.OrderMenuRepository;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.enums.StoreStatus;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private OrderMenuRepository orderMenuRepository;

    @InjectMocks
    private OrderService orderService;

    @Nested
    class createOrder {
        @Test
        void 존재하지_않는_orderId_예외_발생() {
            long userId = 1L;
            long storeId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Store store = new Store(
                    "name", LocalTime.of(9, 0), LocalTime.of(22, 0),
                    1000L, StoreStatus.OPEN, user);
            ReflectionTestUtils.setField(store, "id", storeId);
            Order order = new Order(user, store, OrderState.CART);

            given(orderRepository.findByUserIdAndOrderState(anyLong(),any(OrderState.class)))
                    .willReturn(Optional.empty());

            assertThrows(CustomException.class,
                    () -> orderService.createOrder(userId), "주문 정보를 찾을 수 없습니다");
        }

        @Test
        void 가게가_자정_이후에_닫고_닫은_시간에_주문시_예외_발생() {
            long userId = 1L;
            long storeId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Store store = new Store(
                    "name", LocalTime.of(23, 59), LocalTime.of(1, 0),
                    1000L, StoreStatus.CLOSED_BY_TIME, user);
            ReflectionTestUtils.setField(store, "id", storeId);

            Order order = new Order(user, store, OrderState.CART);

            given(orderRepository.findByUserIdAndOrderState(anyLong(),any(OrderState.class)))
                    .willReturn(Optional.of(order));

            assertThrows(CustomException.class,
                    () -> orderService.createOrder(userId), "가게 운영 시간이 아닙니다");
        }

        @Test
        void 가게가_자정_이전에_닫고_닫은_시간에_주문시_예외_발생() {
            long userId = 1L;
            long storeId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Store store = new Store(
                    "name", LocalTime.of(23, 58), LocalTime.of(23, 59),
                    1000L, StoreStatus.CLOSED_BY_TIME, user);
            ReflectionTestUtils.setField(store, "id", storeId);

            Order order = new Order(user, store, OrderState.CART);

            given(orderRepository.findByUserIdAndOrderState(anyLong(),any(OrderState.class)))
                    .willReturn(Optional.of(order));

            assertThrows(CustomException.class,
                    () -> orderService.createOrder(userId), "가게 운영 시간이 아닙니다");
        }

        @Test
        void 최소_주문_금액보다_낮을시_예외_발생() {
            long userId = 1L;
            long storeId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Store store = new Store(
                    "name", LocalTime.of(0, 0), LocalTime.of(23, 59),
                    15000L, StoreStatus.OPEN, user);
            ReflectionTestUtils.setField(store, "id", storeId);

            Order order = new Order(user, store, OrderState.CART);
            OrderMenu orderMenu = new OrderMenu(order, 1L, "name",100L);
            ReflectionTestUtils.setField(orderMenu, "id", 1L);
            OrderMenu orderMenu1 = new OrderMenu(order, 1L, "name",100L);
            ReflectionTestUtils.setField(orderMenu1, "id", 2L);
            order.addOrderMenu(orderMenu);
            order.addOrderMenu(orderMenu1);

            given(orderRepository.findByUserIdAndOrderState(anyLong(),any(OrderState.class)))
                    .willReturn(Optional.of(order));

            assertThrows(CustomException.class,
                    () -> orderService.createOrder(userId), "최소 주문 금액을 만족해야 주문이 가능합니다");
        }

        @Test
        void 주문_생성_성공() {
            long userId = 1L;
            long storeId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Store store = new Store(
                    "name", LocalTime.of(0, 0), LocalTime.of(23, 59),
                    1000L, StoreStatus.OPEN, user);
            ReflectionTestUtils.setField(store, "id", storeId);

            Order order = new Order(user, store, OrderState.CART);
            OrderMenu orderMenu = new OrderMenu(order, 1L, "name1",1000L);
            ReflectionTestUtils.setField(orderMenu, "id", 1L);
            OrderMenu orderMenu1 = new OrderMenu(order, 1L, "name2",200L);
            ReflectionTestUtils.setField(orderMenu1, "id", 2L);
            order.addOrderMenu(orderMenu);
            order.addOrderMenu(orderMenu1);

            given(orderRepository.findByUserIdAndOrderState(anyLong(),any(OrderState.class)))
                    .willReturn(Optional.of(order));

            OrderResponse response = orderService.createOrder(userId);

            assertNotNull(response);
            assertEquals(storeId, response.getStoreId());
            assertEquals(userId, response.getUserId());
            assertEquals("name1", response.getOrderMenus().get(0).getName());
            assertEquals(1000L, response.getOrderMenus().get(0).getPrice());
            assertEquals(OrderState.PENDING, response.getOrderState());

            verify(orderRepository, times(1)).findByUserIdAndOrderState(anyLong(),any(OrderState.class));
            verify(orderRepository, times(1)).save(any(Order.class));
        }
    }

    @Test
    void userId를_통해_주문_목록을_가져온다() {
        long userId = 1L;
        long storeId = 1L;

        User user = new User("em@em.com", "pw", "name", UserRole.USER);
        User user2 = new User("em2@em.com", "pw", "name2", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        Store store = new Store(
                "name", LocalTime.of(0, 0), LocalTime.of(23, 59),
                1000L, StoreStatus.OPEN, user);
        ReflectionTestUtils.setField(store, "id", storeId);

        long orderId = 1L;
        long order2Id = 2L;
        Order order = new Order(user, store, OrderState.PENDING);
        Order order2 = new Order(user2, store, OrderState.PENDING);
        ReflectionTestUtils.setField(order, "id", orderId);
        ReflectionTestUtils.setField(order2, "id", order2Id);
        List<Order> orderList = List.of(order, order2);

        OrderMenu orderMenu = new OrderMenu(order, 1L, "menu1", 10000L);

        given(orderRepository.findOrdersByUserId(anyLong())).willReturn(orderList);

        List<OrderResponse> list = orderService.getOrdersByUserId(userId);

        assertNotNull(list);
        OrderResponse orderResponse = list.get(0);
        assertEquals(userId, orderResponse.getUserId());
        assertEquals(OrderState.PENDING, orderResponse.getOrderState());
        assertNotNull(orderResponse.getOrderMenus());
    }

    @Nested
    class getOrdersByStoreId {
        @Test
        void store_조회_실패() {
            long userId = 1L;
            long storeId = 1L;

            given(storeRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThrows(CustomException.class,
                    () -> orderService.getOrdersByStoreId(storeId, userId), "가게를 찾을 수 없습니다");
        }

        @Test
        void 해당_가게_사장이_아닌_경우_예외_발생() {
            long userId = 1L;
            long user2Id = 2L;
            long storeId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);
            User user2 = new User("em2@em.com", "pw", "name2", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);
            ReflectionTestUtils.setField(user2, "id", user2Id);

            Store store = new Store(
                    "name", LocalTime.of(0, 0), LocalTime.of(23, 59),
                    1000L, StoreStatus.OPEN, user2);
            ReflectionTestUtils.setField(store, "id", storeId);

            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));

            assertThrows(CustomException.class,
                    () -> orderService.getOrdersByStoreId(storeId, userId), "올바르지 않은 사용자 권한입니다");
        }

        @Test
        void storeId를_통해_주문_목록_조회_성공() {
            long userId = 1L;
            long storeId = 1L;

            User user = new User("em@em.com", "pw", "name", UserRole.USER);
            User user2 = new User("em2@em.com", "pw", "name2", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", userId);

            Store store = new Store(
                    "name", LocalTime.of(0, 0), LocalTime.of(23, 59),
                    1000L, StoreStatus.OPEN, user);
            ReflectionTestUtils.setField(store, "id", storeId);

            long orderId = 1L;
            long order2Id = 2L;
            Order order = new Order(user, store, OrderState.PENDING);
            Order order2 = new Order(user2, store, OrderState.PENDING);
            ReflectionTestUtils.setField(order, "id", orderId);
            ReflectionTestUtils.setField(order2, "id", order2Id);
            List<Order> orderList = List.of(order, order2);

            OrderMenu orderMenu = new OrderMenu(order, 1L, "menu1", 10000L);

            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
            given(orderRepository.findOrdersByStoreId(anyLong())).willReturn(orderList);

            List<OrderResponse> list = orderService.getOrdersByStoreId(storeId, userId);

            assertNotNull(list);
            OrderResponse orderResponse = list.get(0);
            assertEquals(userId, orderResponse.getUserId());
            assertEquals(OrderState.PENDING, orderResponse.getOrderState());
            assertNotNull(orderResponse.getOrderMenus());
        }
    }

    @Nested
    class updateOrderState {
        @Test
        void 주문_정보_조회_실패() {
            long userId = 1L;
            long orderId = 1L;

            given(orderRepository.findById(anyLong())).willReturn(Optional.empty());

            OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.CANCELED);

            assertThrows(CustomException.class,
                    () -> orderService.updateOrderState(userId, orderId, request), "주문 정보를 찾을 수 없습니다");

        }

        @Nested
        class 주문_취소 {
            @Test
            void 주문_취소_실패_잘못된_사용자() {
                Long userId = 1L;
                Long otherUserId = 2L;
                Long orderId = 1L;

                User user = new User("em1@em.com", "pw", "name", UserRole.USER);
                ReflectionTestUtils.setField(user, "id", userId);

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59),
                        1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.PENDING);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getUser(), "id", otherUserId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.CANCELED);

                assertThrows(CustomException.class,
                        () -> orderService.updateOrderState(userId, orderId, request),
                        "올바르지 않은 사용자 권한입니다");
            }

            @Test
            void 주문_취소_실패_잘못된_주문_상태() {
                long userId = 1L;
                long orderId = 1L;

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.ACCEPTED);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getUser(), "id", userId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.CANCELED);

                assertThrows(CustomException.class,
                        () -> orderService.updateOrderState(userId, orderId, request),
                        "배달이 완료되었거나 진행 중인 주문은 취소할 수 없습니다");
            }

            @Test
            void 주문_취소_성공() {
                long userId = 1L;
                long orderId = 1L;

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.PENDING);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getUser(), "id", userId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.CANCELED);

                orderService.updateOrderState(userId, orderId, request);

                assertEquals(OrderState.CANCELED, order.getOrderState());
            }
        }

        @Nested
        class 주문_수락 {
            @Test
            void 주문_수락_실패_잘못된_사용자() {
                Long userId = 1L;
                Long otherUserId = 2L;
                Long orderId = 1L;

                User user = new User("em1@em.com", "pw", "name", UserRole.USER);
                ReflectionTestUtils.setField(user, "id", userId);

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.PENDING);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", otherUserId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.ACCEPTED);

                assertThrows(CustomException.class,
                        () -> orderService.updateOrderState(userId, orderId, request),
                        "올바르지 않은 사용자 권한입니다");
            }

            @Test
            void 주문_수락_실패_잘못된_주문_상태() {
                long userId = 1L;
                long orderId = 1L;

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.ACCEPTED);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", userId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.ACCEPTED);

                assertThrows(CustomException.class,
                        () -> orderService.updateOrderState(userId, orderId, request),
                        "대기 중인 주문만 수락할 수 있습니다");
            }

            @Test
            void 주문_수락_성공() {
                long userId = 1L;
                long orderId = 1L;

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.PENDING);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", userId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.ACCEPTED);

                orderService.updateOrderState(userId, orderId, request);

                assertEquals(OrderState.ACCEPTED, order.getOrderState());
            }
        }

        @Nested
        class 주문_거절 {
            @Test
            void 주문_거절_실패_잘못된_사용자() {
                Long userId = 1L;
                Long otherUserId = 2L;
                Long orderId = 1L;

                User user = new User("em1@em.com", "pw", "name", UserRole.USER);
                ReflectionTestUtils.setField(user, "id", userId);

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.PENDING);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", otherUserId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.REJECTED);

                assertThrows(CustomException.class,
                        () -> orderService.updateOrderState(userId, orderId, request),
                        "올바르지 않은 사용자 권한입니다");
            }

            @Test
            void 주문_거절_실패_잘못된_주문_상태() {
                long userId = 1L;
                long orderId = 1L;

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.ACCEPTED);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", userId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.REJECTED);

                assertThrows(CustomException.class,
                        () -> orderService.updateOrderState(userId, orderId, request),
                        "대기 중인 주문만 거절할 수 있습니다");
            }

            @Test
            void 주문_거절_성공() {
                long userId = 1L;
                long orderId = 1L;

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.PENDING);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", userId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.REJECTED);

                orderService.updateOrderState(userId, orderId, request);

                assertEquals(OrderState.REJECTED, order.getOrderState());
            }
        }

        @Nested
        class 배달_시작 {
            @Test
            void 배달_시작_실패_잘못된_사용자() {
                Long userId = 1L;
                Long otherUserId = 2L;
                Long orderId = 1L;

                User user = new User("em1@em.com", "pw", "name", UserRole.USER);
                ReflectionTestUtils.setField(user, "id", userId);

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.ACCEPTED);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", otherUserId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.DELIVERY);

                assertThrows(CustomException.class,
                        () -> orderService.updateOrderState(userId, orderId, request),
                        "올바르지 않은 사용자 권한입니다");
            }

            @Test
            void 배달_시작_실패_잘못된_주문_상태() {
                long userId = 1L;
                long orderId = 1L;

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.PENDING);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", userId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.DELIVERY);

                assertThrows(CustomException.class,
                        () -> orderService.updateOrderState(userId, orderId, request),
                        "수락된 주문만 배달을 시작할 수 있습니다");
            }

            @Test
            void 배달_시작_성공() {
                long userId = 1L;
                long orderId = 1L;

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.ACCEPTED);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", userId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.DELIVERY);

                orderService.updateOrderState(userId, orderId, request);

                assertEquals(OrderState.DELIVERY, order.getOrderState());
            }
        }

        @Nested
        class 배달_완료 {
            @Test
            void 배달_완료_실패_잘못된_사용자() {
                Long userId = 1L;
                Long otherUserId = 2L;
                Long orderId = 1L;

                User user = new User("em1@em.com", "pw", "name", UserRole.USER);
                ReflectionTestUtils.setField(user, "id", userId);

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.DELIVERY);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", otherUserId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.COMPLETED);

                assertThrows(CustomException.class,
                        () -> orderService.updateOrderState(userId, orderId, request), "올바르지 않은 사용자 권한입니다");
            }

            @Test
            void 배달_완료_실패_잘못된_주문_상태() {
                long userId = 1L;
                long orderId = 1L;

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.PENDING);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", userId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.COMPLETED);

                assertThrows(CustomException.class,
                        () -> orderService.updateOrderState(userId, orderId, request),
                        "배달 시작된 주문만 배달 완료 처리할 수 있습니다");
            }

            @Test
            void 배달_완료_성공() {
                long userId = 1L;
                long orderId = 1L;

                Order order = new Order(new User("test@email.com", "pw", "test", UserRole.USER)
                        , new Store("store", LocalTime.of(0, 0), LocalTime.of(23, 59)
                        , 1000L, StoreStatus.OPEN
                        , new User("em2@em.com", "pw", "name", UserRole.USER)), OrderState.DELIVERY);
                ReflectionTestUtils.setField(order, "id", orderId);
                ReflectionTestUtils.setField(order.getStore().getUser(), "id", userId);

                given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

                OrderStateUpdateRequest request = new OrderStateUpdateRequest(OrderState.COMPLETED);

                orderService.updateOrderState(userId, orderId, request);

                assertEquals(OrderState.COMPLETED, order.getOrderState());
            }
        }
    }

}
