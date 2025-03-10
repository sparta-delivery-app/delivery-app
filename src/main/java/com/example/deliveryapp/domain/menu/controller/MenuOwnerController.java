package com.example.deliveryapp.domain.menu.controller;

import com.example.deliveryapp.domain.common.annotation.Auth;
import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.menu.dto.request.MenuRequest;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponse;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponseWithImageUrl;
import com.example.deliveryapp.domain.menu.service.MenuOwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/menus")
public class MenuOwnerController {

    private final MenuOwnerService menuOwnerService;

    @PostMapping
    public ResponseEntity<MenuResponse> saveMenu(
            @Auth AuthUser authUser,
            @PathVariable Long storeId,
            @Valid @RequestBody MenuRequest request
    ) {
        MenuResponse response = menuOwnerService.saveMenu(authUser.getId(), storeId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponse> updateMenu(
            @Auth AuthUser authUser,
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @Valid @RequestBody MenuRequest request
    ) {
        MenuResponse response = menuOwnerService.updateMenu(authUser.getId(), storeId, menuId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @Auth AuthUser authUser,
            @PathVariable Long storeId,
            @PathVariable Long menuId
    ) {
        menuOwnerService.deleteMenu(authUser.getId(), storeId, menuId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{menuId}/images")
    public ResponseEntity<MenuResponseWithImageUrl> uploadMenuImage(
            @Auth AuthUser authUser,
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestParam MultipartFile file
    ) {
        MenuResponseWithImageUrl response = menuOwnerService.uploadMenuImage(authUser.getId(), storeId, menuId, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{menuId}/images")
    public ResponseEntity<Void> deleteMenuImage(
            @Auth AuthUser authUser,
            @PathVariable Long storeId,
            @PathVariable Long menuId
    ) {
        menuOwnerService.deleteMenuImage(authUser.getId(), storeId, menuId);
        return ResponseEntity.ok().build();
    }
}
