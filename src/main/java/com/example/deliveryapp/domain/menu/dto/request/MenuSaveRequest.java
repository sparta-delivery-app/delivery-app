package com.example.deliveryapp.domain.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuSaveRequest {
    @NotBlank
    @Size(max = 255)
    private final String menuName;

    @NotNull
    private final Long price;
}
