package com.example.deliveryapp.domain.menu.converter;

import com.example.deliveryapp.domain.menu.dto.request.OptionCategoryRequest;
import com.example.deliveryapp.domain.menu.dto.request.OptionItemRequest;
import com.example.deliveryapp.domain.menu.dto.response.OptionCategoryResponse;
import com.example.deliveryapp.domain.menu.dto.response.OptionItemResponse;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.entity.OptionCategory;
import com.example.deliveryapp.domain.menu.entity.OptionItem;

import java.util.List;

public class OptionCategoryConverter {
    public static OptionCategory toEntity(OptionCategoryRequest request, Menu menu) {
        OptionCategory optionCategory = OptionCategory.builder()
                .name(request.getOptionCategoryName())
                .isRequired(request.getIsRequired())
                .isMultiple(request.getIsMultiple())
                .maxOptions(request.getMaxOptions())
                .menu(menu)
                .build();

        request.getOptionItems().stream()
                .map(OptionCategoryConverter::toEntity)
                .forEach(optionCategory::addOptionItem);

        return optionCategory;
    }

    public static OptionItem toEntity(OptionItemRequest request) {
        return OptionItem.builder()
                .name(request.getOptionItemName())
                .additionalPrice(request.getAdditionalPrice())
                .build();
    }

    public static OptionCategoryResponse toResponse(OptionCategory optionCategory) {
        List<OptionItemResponse> optionItemResponses = optionCategory.getOptionItems().stream()
                .map(OptionCategoryConverter::toResponse)
                .toList();

        return new OptionCategoryResponse(
                optionCategory.getId(),
                optionCategory.getName(),
                optionCategory.getIsRequired(),
                optionCategory.getIsMultiple(),
                optionCategory.getMaxOptions(),
                optionItemResponses
        );
    }

    public static OptionItemResponse toResponse(OptionItem optionItem) {
        return new OptionItemResponse(
                optionItem.getId(),
                optionItem.getName(),
                optionItem.getAdditionalPrice()
        );
    }
}
