package com.example.deliveryapp.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderMenuRequest {

    @NotBlank
    private Long menuId;

    @NotBlank
    private String name;

    @NotBlank
    private Long price;
}
