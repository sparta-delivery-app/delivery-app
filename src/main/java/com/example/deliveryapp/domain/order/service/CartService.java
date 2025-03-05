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
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public void addCart(Long userId, Long menuId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Menu menu = menuRepository.findById(menuId).orElseThrow(
                () -> new CustomException(ErrorCode.MENU_NOT_FOUND));
        Store store = menu.getStore();

        Order order = orderRepository.findByUserIdAndOrderState(userId, OrderState.CART).orElse(null);

        if (order != null) { // 장바구니 이미 존재하는 경우
            if (!store.getId().equals(order.getStore().getId())) {
                order.getOrderMenus().clear(); // 장바구니 가게 변경 시 장바구니 초기화
                order.setStore(store); // 장바구니 가게 변경 시 요청사항의 가게로 변경
            }
            OrderMenu orderMenu = new OrderMenu(order, menuId, menu.getName(), menu.getPrice());
            order.addOrderMenu(orderMenu);
            orderRepository.save(order);
        } else {
            Order newOrder = new Order(user, store, OrderState.CART);
            OrderMenu orderMenu = new OrderMenu(newOrder, menuId, menu.getName(), menu.getPrice());

            newOrder.addOrderMenu(orderMenu);
            orderRepository.save(newOrder);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // 자정에 장바구니 검사
    public void cleanupCarts() {
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            checkCartTimeOut(order.getId());
        }
    }

    private void checkCartTimeOut(Long orderId) {
        Order order = orderRepository.findByUserIdAndOrderState(orderId, OrderState.CART)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        Duration duration = Duration.between(order.getUpdatedAt(), LocalDateTime.now());

        if (24 <= duration.toHours()) {
            order.getOrderMenus().clear();
            orderRepository.save(order);
        }
    }
}
