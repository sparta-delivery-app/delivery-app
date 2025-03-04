package com.example.deliveryapp.domain.order.dto.request;

import com.example.deliveryapp.domain.order.enums.OrderState;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderStateUpdateRequest {

    @NotBlank
    private OrderState orderState;
}
