package com.example.deliveryapp.domain.menu.controller;

import com.example.deliveryapp.domain.menu.dto.response.OptionCategoryPageResponse;
import com.example.deliveryapp.domain.menu.service.MenuOptionService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/menus/{menuId}/option-categories")
public class MenuOptionController {

    private static final String DEFAULT_SIZE = "10";

    private final MenuOptionService menuOptionService;

    @GetMapping
    public ResponseEntity<OptionCategoryPageResponse> getMenuOptions(
            @PathVariable Long menuId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) Integer size
    ) {
        OptionCategoryPageResponse response = menuOptionService.getMenuOptions(menuId, page, size);
        return ResponseEntity.ok(response);
    }
}
