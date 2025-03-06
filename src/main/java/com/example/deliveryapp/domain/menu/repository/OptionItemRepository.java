package com.example.deliveryapp.domain.menu.repository;

import com.example.deliveryapp.domain.menu.entity.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionItemRepository extends JpaRepository<OptionItem, Long> {
}
