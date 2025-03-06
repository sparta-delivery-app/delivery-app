package com.example.deliveryapp.domain.menu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OptionCategoryRequest {
    @NotBlank
    private final String optionCategoryName;
    @NotNull
    private final Boolean isRequired;
    @NotNull
    private final Boolean isMultiple;
    @Min(1)
    private final Integer maxOptions;
    @NotEmpty
    @Valid
    private final List<OptionItemRequest> optionItems;
}
