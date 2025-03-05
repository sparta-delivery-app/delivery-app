package com.example.deliveryapp.domain.order.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.order.dto.request.OrderMenuRequest;
import com.example.deliveryapp.domain.order.dto.request.OrderStateUpdateRequest;
import com.example.deliveryapp.domain.order.dto.response.OrderMenuResponse;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.entity.OrderMenu;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.order.repository.OrderMenuRepository;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final OrderMenuRepository orderMenuRepository;

    @Transactional
    public void addCart(Long storeId, Long userId, OrderMenuRequest request) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Order order = orderRepository.findByUserIdAndOrderState(userId, OrderState.CART).orElseThrow(null);

        if (order != null) { // 장바구니 이미 존재하는 경우
            if (!storeId.equals(order.getStore().getId())) {
                order.getOrderMenus().clear(); // 장바구니 가게 변경 시 장바구니 초기화
                order.setStore(store); // 장바구니 가게 변경 시 요청사항의 가게로 변경
            }
            OrderMenu orderMenu = new OrderMenu(order, request.getMenuId(), request.getName(), request.getPrice());
            order.addOrderMenu(orderMenu);
            orderRepository.save(order);
        } else {
            Order newOrder = new Order(user, store, OrderState.CART);
            OrderMenu orderMenu = new OrderMenu(newOrder, request.getMenuId(), request.getName(), request.getPrice());

            newOrder.addOrderMenu(orderMenu);
            orderRepository.save(newOrder);
        }
    }

    @Transactional
    public OrderResponse createOrder(Long userId) {
        Order order = orderRepository.findByUserIdAndOrderState(userId, OrderState.CART)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        Store store = order.getStore();

        LocalTime nowTime = LocalTime.now();
        LocalTime openTime = store.getOpenTime();
        LocalTime closeTime = store.getCloseTime();

        boolean isClosedToday = nowTime.isBefore(openTime) || nowTime.isAfter(closeTime);

        if (isClosedToday) {
            throw new CustomException(ErrorCode.ORDER_CLOSED);
        }

        if (order.calculateTotalPrice() < store.getMinimumOrderPrice()) {
            throw new CustomException(ErrorCode.ORDER_TOO_CHEAP);
        }

        order.setOrderState(OrderState.PENDING);

        List<OrderMenuResponse> orderMenus = order.getOrderMenus().stream()
                .map(menu -> new OrderMenuResponse(menu.getMenuId(), menu.getName(), menu.getPrice()))
                .collect(toList());

        orderRepository.save(order);

        return new OrderResponse(order, orderMenus);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orderList = orderRepository.findOrdersByUserId(userId);

        return orderList.stream()
                .map(order -> {
                    List<OrderMenuResponse> orderMenus = order.getOrderMenus().stream()
                            .map(menu -> new OrderMenuResponse(menu.getMenuId(), menu.getName(), menu.getPrice()))
                            .toList();
                    return new OrderResponse(order, orderMenus);
                }).collect(toList());
    }

    @Transactional
    public List<OrderResponse> getOrdersByStoreId(Long storeId, Long userId) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 해당 가게 사장이 아닌 경우
        if (!userId.equals(store.getUser().getId())) {
            throw new CustomException(ErrorCode.INVALID_USER_ROLE);
        }

        List<Order> orderList = orderRepository.findOrdersByStoreId(storeId);
        return orderList.stream()
                .map(order -> {
                    List<OrderMenuResponse> orderMenus = order.getOrderMenus().stream()
                            .map(menu -> new OrderMenuResponse(menu.getMenuId(), menu.getName(), menu.getPrice()))
                            .toList();
                    return new OrderResponse(order, orderMenus);
                }).collect(toList());
    }

    @Transactional
    public void updateOrderState(Long userId, Long orderId, OrderStateUpdateRequest request) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        Long orderUserId = order.getUser().getId();
        Long storeOwnerId = order.getStore().getUser().getId();
        OrderState orderState = request.getOrderState();

        switch (orderState) {
            case CANCELED:
                validateCancelOrder(userId, order, orderUserId);
                order.setOrderState(OrderState.CANCELED);
                break;
            case ACCEPTED:
                validateAcceptOrder(userId, order, storeOwnerId);
                order.setOrderState(OrderState.ACCEPTED);
                break;
            case REJECTED:
                validateRejectOrder(userId, order, storeOwnerId);
                order.setOrderState(OrderState.REJECTED);
                break;
            case DELIVERY:
                validateStartDelivery(userId, order, storeOwnerId);
                order.setOrderState(OrderState.DELIVERY);
                break;
            case COMPLETED:
                validateCompleteDelivery(userId, order, storeOwnerId);
                order.setOrderState(OrderState.COMPLETED);
                break;
        }
    }

    // 주문 취소 상태 검증
    private void validateCancelOrder(Long userId, Order order, Long orderUserId) {
        if (!orderUserId.equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_USER_ROLE);
        }
        if (!OrderState.PENDING.equals(order.getOrderState())) {
            throw new CustomException(ErrorCode.ORDER_CANNOT_BE_CANCELED);
        }
    }

    // 주문 수락 상태 검증
    private void validateAcceptOrder(Long userId, Order order, Long storeOwnerId) {
        if (!storeOwnerId.equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_USER_ROLE);
        }
        if (!OrderState.PENDING.equals(order.getOrderState())) {
            throw new CustomException(ErrorCode.ORDER_CANNOT_BE_ACCEPTED);
        }
    }

    // 주문 거절 상태 검증
    private void validateRejectOrder(Long userId, Order order, Long storeOwnerId) {
        if (!storeOwnerId.equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_USER_ROLE);
        }
        if (!OrderState.PENDING.equals(order.getOrderState())) {
            throw new CustomException(ErrorCode.ORDER_CANNOT_BE_REJECTED);
        }
    }

    // 배달 시작 상태 검증
    private void validateStartDelivery(Long userId, Order order, Long storeOwnerId) {
        if (!storeOwnerId.equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_USER_ROLE);
        }
        if (!OrderState.ACCEPTED.equals(order.getOrderState())) {
            throw new CustomException(ErrorCode.ORDER_CANNOT_BE_DELIVERY);
        }
    }

    // 배달 완료 상태 검증
    private void validateCompleteDelivery(Long userId, Order order, Long storeOwnerId) {
        if (!storeOwnerId.equals(userId)) {
            throw new CustomException(ErrorCode.INVALID_USER_ROLE);
        }
        if (!OrderState.DELIVERY.equals(order.getOrderState())) {
            throw new CustomException(ErrorCode.ORDER_CANNOT_BE_COMPLETED);
        }
    }

}
