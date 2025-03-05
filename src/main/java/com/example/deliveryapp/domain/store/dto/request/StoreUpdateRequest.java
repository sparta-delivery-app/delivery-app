package com.example.deliveryapp.domain.store.dto.request;

import lombok.Getter;

@Getter
public class StoreUpdateRequest {

    private final String name;
    private final String openTime;
    private final String closeTime;
    private final Long minimumOrderPrice;
    private final String status;

    public StoreUpdateRequest(String name, String openTime, String closeTime, Long minimumOrderPrice, String status) {
        this.name = name;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.minimumOrderPrice = minimumOrderPrice;
        this.status = status;
    }
}
