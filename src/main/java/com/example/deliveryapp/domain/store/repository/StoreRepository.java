package com.example.deliveryapp.domain.store.repository;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT s.user.id FROM Store s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Long> findOwnerIdByStoreIdIfActive(@Param("id") Long id);

    boolean existsByIdAndDeletedAtIsNull(Long id);

    default Store findActiveStoreByIdOrThrow(Long id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
    }

    default Long findOwnerIdByStoreIdOrThrow(Long id) {
        return findOwnerIdByStoreIdIfActive(id)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
    }
    List<Store> findAllByUser(User user);

    @Modifying
    @Transactional
    @Query("UPDATE Store s SET s.isDeleted = true WHERE s.user.id = :id")
    int softdeleteByUserId(Long id);

    long countByUserIdAndIsDeletedFalse(Long userId);

    void deleteByUserId(Long userId);
}
