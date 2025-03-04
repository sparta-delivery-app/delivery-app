package com.example.deliveryapp.domain.menu.repository;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByIdAndDeletedAtIsNull(Long id);

    default Menu findActiveMenuByIdOrThrow(Long id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
    }
}
