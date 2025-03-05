package com.example.deliveryapp.domain.store.dto.request;

import com.example.deliveryapp.domain.store.consts.Const;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StoreUpdateRequest {

    @NotBlank(message = Const.STORE_NAME_NOT_NULL)
    @Size(max = 20, message = Const.STORE_NAME_SIZE)
    private final String name;

    @NotBlank(message = Const.OPEN_TIME_NOT_NULL)
    @Pattern(regexp = Const.OPEN_TIME_PATTERN, message = Const.OPEN_TIME_REQUIREMENT)
    private final String openTime;

    @NotBlank(message = Const.CLOSE_TIME_NOT_NULL)
    @Pattern(regexp = Const.CLOSE_TIME_PATTERN, message = Const.CLOSE_TIME_REQUIREMENT)
    private final String closeTime;

    @NotBlank(message = Const.MINIMUM_ORDER_PRICE_NOT_NULL)
    @Min(value = 0, message = Const.MINIMUM_ORDER_PRICE_REQUIREMENT)
    private final Long minimumOrderPrice;

    @NotBlank(message = Const.STORE_STATUS_NOT_NULL)
    private final String status;

    @Builder
    public StoreUpdateRequest(String name, String openTime, String closeTime, Long minimumOrderPrice, String status) {
        this.name = name;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.minimumOrderPrice = minimumOrderPrice;
        this.status = status;
    }
}
