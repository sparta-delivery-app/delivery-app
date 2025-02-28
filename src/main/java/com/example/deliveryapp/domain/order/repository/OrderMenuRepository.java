package com.example.deliveryapp.domain.order.repository;

import com.example.deliveryapp.domain.order.entity.OrderMenu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderMenuRepository extends JpaRepository<OrderMenu, Long> {
}
