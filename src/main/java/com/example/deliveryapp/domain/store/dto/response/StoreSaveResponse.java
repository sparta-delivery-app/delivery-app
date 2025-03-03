package com.example.deliveryapp.domain.store.dto.response;

import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.enums.StoreStatus;
import com.example.deliveryapp.domain.user.entity.User;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
public class StoreSaveResponse {
    private final Long id;
    private final Long userId;
    private final String name;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final Long minimumOrderPrice;
    private final StoreStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public StoreSaveResponse(Long id, Long userId, String name, LocalTime openTime, LocalTime closeTime, Long minimumOrderPrice, StoreStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.minimumOrderPrice = minimumOrderPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static StoreSaveResponse of(Store store, User user) {
        return new StoreSaveResponse(
                store.getId(),
                user.getId(),
                store.getName(),
                store.getOpenTime(),
                store.getCloseTime(),
                store.getMinimumOrderPrice(),
                store.getStatus(),
                store.getCreatedAt(),
                store.getUpdatedAt(),
                store.getDeletedAt());
    }
}
