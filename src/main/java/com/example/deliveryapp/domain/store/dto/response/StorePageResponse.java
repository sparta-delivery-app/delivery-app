package com.example.deliveryapp.domain.store.dto.response;

import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.enums.StoreStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class StorePageResponse {
    private final Long id;
    private final String name;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final Long minimumOrderPrice;
    private final StoreStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
    private final Double averageRating;
    private final Long reviewCount;

    public StorePageResponse(Long id, String name, LocalTime openTime, LocalTime closeTime, Long minimumOrderPrice, StoreStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, Double averageRating, Long reviewCount) {
        this.id = id;
        this.name = name;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.minimumOrderPrice = minimumOrderPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;

    }

    public static StorePageResponse of(Store store, Double averageRating, Long reviewCount) {
        return new StorePageResponse(
                store.getId(),
                store.getName(),
                store.getOpenTime(),
                store.getCloseTime(),
                store.getMinimumOrderPrice(),
                store.getStatus(),
                store.getCreatedAt(),
                store.getUpdatedAt(),
                store.getDeletedAt(),
                averageRating,
                reviewCount
        );
    }
}
