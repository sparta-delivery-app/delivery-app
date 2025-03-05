package com.example.deliveryapp.domain.order.repository;

import com.example.deliveryapp.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrdersByUserId(Long userId);

    List<Order> findOrdersByStoreId(Long storeId);

    Long findStoreIdById(Long orderId);
}
