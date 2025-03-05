package com.example.deliveryapp.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OptionCategoryResponse {
    private final Long optionCategoryId;
    private final String optionCategoryName;
    private final Boolean isRequired;
    private final Boolean isMultiple;
    private final Integer maxOptions;
    private final List<OptionItemResponse> optionItems;
}
