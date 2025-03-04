package com.example.deliveryapp.domain.menu.controller;

import com.example.deliveryapp.domain.common.annotation.Auth;
import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.menu.dto.request.MenuSaveRequest;
import com.example.deliveryapp.domain.menu.dto.request.MenuUpdateRequest;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponse;
import com.example.deliveryapp.domain.menu.service.MenuOwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/menus")
public class MenuOwnerController {

    private final MenuOwnerService menuOwnerService;

    @PostMapping
    public ResponseEntity<MenuResponse> saveMenu(
            @Auth AuthUser authUser,
            @PathVariable Long storeId,
            @Valid @RequestBody MenuSaveRequest request
    ) {
        MenuResponse response = menuOwnerService.saveMenu(authUser.getId(), storeId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(
            @Auth AuthUser authUser,
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @Valid @RequestBody MenuUpdateRequest request
    ) {
        MenuResponse response = menuOwnerService.updateMenu(authUser.getId(), storeId, menuId, request);
        return ResponseEntity.ok(response);
    }
}
