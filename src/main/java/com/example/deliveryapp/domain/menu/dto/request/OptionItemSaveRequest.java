package com.example.deliveryapp.domain.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OptionItemSaveRequest {
    @NotBlank
    private final String optionItemName;
    @NotNull
    private final Long additionalPrice;
}
