package com.example.deliveryapp.domain.store.repository;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

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

    long countByUserIdAndIsDeletedFalse(Long userId);

}
