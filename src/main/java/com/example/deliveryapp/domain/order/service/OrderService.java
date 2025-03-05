package com.example.deliveryapp.domain.order.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.order.dto.request.OrderRequest;
import com.example.deliveryapp.domain.order.dto.request.OrderStateUpdateRequest;
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
    public OrderResponse createOrder(Long userId, OrderRequest orderRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Store store = storeRepository.findById(orderRequest.getStoreId())
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        LocalTime nowTime = LocalTime.now();
        LocalTime openTime = store.getOpenTime();
        LocalTime closeTime = store.getCloseTime();

        boolean isClosedToday = nowTime.isBefore(openTime) || nowTime.isAfter(closeTime);

        if (isClosedToday) {
            throw new CustomException(ErrorCode.ORDER_CLOSED);
        }

        if (orderRequest.getOrderMenus().getPrice() < store.getMinimumOrderPrice()) {
            throw new CustomException(ErrorCode.ORDER_TOO_CHEAP);
        }

        Order order = new Order(user, store, OrderState.PENDING);

        orderRepository.save(order);

        OrderMenu orderMenu = new OrderMenu(order,
                orderRequest.getOrderMenus().getMenuId(),
                orderRequest.getOrderMenus().getName(),
                orderRequest.getOrderMenus().getPrice());

        orderMenuRepository.save(orderMenu);

        return new OrderResponse(order, orderMenu);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orderList = orderRepository.findOrdersByUserId(userId);

        return orderList.stream()
                .map(order -> {
                    OrderMenu orderMenu = orderMenuRepository.findByOrderId(order.getId());
                    return new OrderResponse(order, orderMenu);
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
                    OrderMenu orderMenu = orderMenuRepository.findByOrderId(order.getId());
                    return new OrderResponse(order, orderMenu);
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
