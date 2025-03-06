package com.example.deliveryapp.domain.order.repository;

import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.enums.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrdersByUserId(Long userId);

    List<Order> findOrdersByStoreId(Long storeId);

    @Query("SELECT o.store.id FROM Order o WHERE o.id = :orderId")
    Long findStoreIdById(Long orderId);

    boolean existsByStoreId(Long storeId);

    Optional<Order> findByUserIdAndOrderState(Long userId, OrderState orderState);
}
