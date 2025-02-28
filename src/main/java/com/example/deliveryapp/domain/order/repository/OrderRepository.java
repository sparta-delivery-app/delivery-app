package com.example.deliveryapp.domain.order.repository;

import com.example.deliveryapp.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
