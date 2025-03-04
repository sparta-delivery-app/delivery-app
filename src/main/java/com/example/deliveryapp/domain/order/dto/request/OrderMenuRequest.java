package com.example.deliveryapp.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderMenuRequest {

    @NotNull
    private Long menuId;

    @NotBlank
    private String name;

    @NotNull
    private Long price;
}
