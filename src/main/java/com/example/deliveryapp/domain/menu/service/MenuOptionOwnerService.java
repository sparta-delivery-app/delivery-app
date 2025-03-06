package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.converter.OptionCategoryConverter;
import com.example.deliveryapp.domain.menu.dto.request.OptionCategorySaveRequest;
import com.example.deliveryapp.domain.menu.dto.response.OptionCategoryResponse;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.entity.OptionCategory;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.menu.repository.OptionCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuOptionOwnerService {

    private final MenuRepository menuRepository;
    private final OptionCategoryRepository optionCategoryRepository;

    public OptionCategoryResponse saveMenuOption(Long userId, Long menuId, OptionCategorySaveRequest request) {
        validateMenuOwner(userId, menuId);
        Menu menu = menuRepository.findActiveMenuByIdOrThrow(menuId);

        OptionCategory optionCategory = OptionCategoryConverter.toEntity(request, menu);
        optionCategoryRepository.save(optionCategory);

        return OptionCategoryConverter.toResponse(optionCategory);
    }

    private void validateMenuOwner(Long userId, Long menuId) {
        Long ownerId = menuRepository.findOwnerIdByMenuIdOrThrow(menuId);
        if (!ownerId.equals(userId)) {
            throw new CustomException(ErrorCode.NOT_STORE_OWNER);
        }
    }
}
