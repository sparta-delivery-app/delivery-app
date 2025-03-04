package com.example.deliveryapp.domain.store.enums;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;

import java.util.Arrays;

public enum StoreStatus {
    OPEN, CLOSED_BY_TIME, PERMANENTLY_CLOSED;

    public static StoreStatus of(String status) {
        return Arrays.stream(StoreStatus.values())
                .filter(s -> s.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_STORE_STATUS));
    }
}
