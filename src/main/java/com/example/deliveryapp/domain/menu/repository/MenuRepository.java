package com.example.deliveryapp.domain.menu.repository;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponse;
import com.example.deliveryapp.domain.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByIdAndDeletedAtIsNull(Long id);

    Page<Menu> findAllByStoreIdAndDeletedAtIsNull(Long storeId, Pageable pageable);

    default Menu findActiveMenuByIdOrThrow(Long id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
    }

    @Query("SELECT new com.example.deliveryapp.domain.menu.dto.response.MenuResponse(m.id, m.name, m.price, m.description) " +
            "FROM Menu m " +
            "WHERE m.store.id = :storeId AND m.deletedAt IS NULL")
    List<MenuResponse> findListByStoreId(@Param("storeId") Long storeId);
}
