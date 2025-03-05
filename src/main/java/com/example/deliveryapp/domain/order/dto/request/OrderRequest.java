package com.example.deliveryapp.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderRequest {

    @NotNull
    private Long storeId;

    private OrderMenuRequest orderMenus;
}
