package com.example.deliveryapp.domain.order.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.order.converter.OrderConverter;
import com.example.deliveryapp.domain.order.dto.request.OrderStateUpdateRequest;
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

@Service
@RequiredArgsConstructor
public class OrderService {

    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;

    // TODO: 메뉴나 메뉴 항목 존재하지 않을 때 예외 처리 추가
    @Transactional
    public OrderResponse createOrder(Long userId) {
        Order order = orderRepository.findByUserIdAndOrderState(userId, OrderState.CART)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_NOT_FOUND));
        validateOrderAvailability(order);

        order.setOrderState(OrderState.PENDING);

        return OrderConverter.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findOrdersByUserId(userId).stream()
                .filter(order -> !order.getOrderState().equals(OrderState.CART))
                .map(OrderConverter::toResponse)
                .toList();
    }

    @Transactional
    public List<OrderResponse> getOrdersByStoreId(Long userId, Long storeId) {
        Store store = storeRepository.findActiveStoreByIdOrThrow(storeId);
        validateStoreOwner(userId, store.getUser().getId());

        return orderRepository.findOrdersByStoreId(storeId).stream()
                .filter(order -> !order.getOrderState().equals(OrderState.CART))
                .map(OrderConverter::toResponse)
                .toList();
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

    private static void validateOrderAvailability(Order order) {
        Store store = order.getStore();
        if (store.getStatus() == StoreStatus.PERMANENTLY_CLOSED) {
            throw new CustomException(ErrorCode.STORE_ALREADY_CLOSED); // 폐업 상태 예외 처리
        }

        LocalTime now = LocalTime.now();
        if (now.isBefore(store.getOpenTime()) || now.isAfter(store.getCloseTime())) {
            throw new CustomException(ErrorCode.ORDER_CLOSED);
        }

        if (order.calculateTotalPrice() < store.getMinimumOrderPrice()) {
            throw new CustomException(ErrorCode.ORDER_TOO_CHEAP);
        }
    }

    private static void validateStoreOwner(Long userId, Long storeOwnerId) {
        if (!userId.equals(storeOwnerId)) {
            throw new CustomException(ErrorCode.INVALID_USER_ROLE);
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
