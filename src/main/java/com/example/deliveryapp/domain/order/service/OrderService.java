package com.example.deliveryapp.domain.order.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.order.dto.request.OrderStateUpdateRequest;
import com.example.deliveryapp.domain.order.dto.response.OrderMenuResponse;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.enums.StoreStatus;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(Long userId) {
        Order order = orderRepository.findByUserIdAndOrderState(userId, OrderState.CART)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        Store store = order.getStore();

        if (store.getStatus() == StoreStatus.PERMANENTLY_CLOSED) {
            throw new CustomException(ErrorCode.STORE_ALREADY_CLOSED); // 폐업 상태 예외 처리
        }

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
