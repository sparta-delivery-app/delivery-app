package com.example.deliveryapp.domain.store.dto;

import lombok.Getter;

@Getter
public class ReviewStatistics {

    private final Long storeId;
    private final Long count;
    private final Double averageRating;

    public ReviewStatistics(Long storeId, Long count, Double averageRating) {
        this.storeId = storeId;
        this.count = count;
        this.averageRating = averageRating;
    }
}
