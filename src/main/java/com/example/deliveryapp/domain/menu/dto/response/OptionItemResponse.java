package com.example.deliveryapp.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OptionItemResponse {
    private final Long optionItemId;
    private final String optionItemName;
    private final Long additionalPrice;
}
