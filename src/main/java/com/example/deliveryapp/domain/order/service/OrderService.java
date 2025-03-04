package com.example.deliveryapp.domain.order.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.order.dto.request.OrderRequest;
import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.entity.OrderMenu;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.order.repository.OrderMenuRepository;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(()->new CustomException(ErrorCode.USER_NOT_FOUND));
        Store store = storeRepository.findById(orderRequest.getStoreId())
                .orElseThrow(()->new CustomException(ErrorCode.STORE_NOT_FOUND));

        Order order = new Order(user,store, OrderState.PENDING);

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
                .map(order->{
                    OrderMenu orderMenu = orderMenuRepository.findByOrderId(order.getId());
                    return new OrderResponse(order,orderMenu);
                }).collect(toList());
    }

    @Transactional
    public List<OrderResponse> getOrdersByStoreId(Long storeId, Long userId, UserRole userRole) {
        if(!(UserRole.OWNER).equals(userRole)) {
            throw new CustomException(ErrorCode.INVALID_USER_ROLE);
        }

        List<Order> orderList = orderRepository.findOrdersByStoreId(storeId);
        return orderList.stream()
                .map(order->{
                    OrderMenu orderMenu = orderMenuRepository.findByOrderId(order.getId());
                    return new OrderResponse(order,orderMenu);
                }).collect(toList());
    }
}
