package com.example.deliveryapp.domain.menu.controller;

import com.example.deliveryapp.domain.menu.dto.response.MenuPageResponse;
import com.example.deliveryapp.domain.menu.service.MenuService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/menus")
public class MenuController {

    private static final String DEFAULT_SIZE = "10";

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<MenuPageResponse> getMenus(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = DEFAULT_SIZE) @Min(1) Integer size
    ) {
        MenuPageResponse response = menuService.getMenus(storeId, page, size);
        return ResponseEntity.ok(response);
    }
}
