package com.example.deliveryapp.domain.menu.controller;

import com.example.deliveryapp.domain.common.annotation.Auth;
import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.menu.dto.request.OptionCategorySaveRequest;
import com.example.deliveryapp.domain.menu.dto.response.OptionCategoryResponse;
import com.example.deliveryapp.domain.menu.service.MenuOptionOwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/menus/{menuId}/option-categories")
public class MenuOptionOwnerController {

    private final MenuOptionOwnerService menuOptionOwnerService;

    @PostMapping
    public ResponseEntity<OptionCategoryResponse> saveMenuOption(
            @Auth AuthUser authUser,
            @PathVariable Long menuId,
            @Valid @RequestBody OptionCategorySaveRequest request
    ) {
        OptionCategoryResponse response = menuOptionOwnerService.saveMenuOption(authUser.getId(), menuId, request);
        return ResponseEntity.ok(response);
    }
}
