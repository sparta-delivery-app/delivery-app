package com.example.deliveryapp.domain.order.dto.request;

import com.example.deliveryapp.domain.order.enums.OrderState;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderStateUpdateRequest {

    @NotNull
    private OrderState orderState;
}
