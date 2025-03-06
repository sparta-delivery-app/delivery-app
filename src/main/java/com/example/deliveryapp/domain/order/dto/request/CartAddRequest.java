package com.example.deliveryapp.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CartAddRequest {
    @NotNull
    private final Long menuId;
    @Valid
    private final List<OptionRequest> options;

    @Getter
    @AllArgsConstructor
    public static class OptionRequest {
        @NotNull
        private final Long optionCategoryId;

        private final List<Long> optionItemIds;
    }
}
