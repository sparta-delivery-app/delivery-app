package com.example.deliveryapp.domain.menu.repository;

import com.example.deliveryapp.domain.menu.entity.OptionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionCategoryRepository extends JpaRepository<OptionCategory, Long> {
}
