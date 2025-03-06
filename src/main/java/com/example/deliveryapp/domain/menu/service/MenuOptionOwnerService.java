package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.converter.OptionCategoryConverter;
import com.example.deliveryapp.domain.menu.dto.request.OptionCategoryRequest;
import com.example.deliveryapp.domain.menu.dto.response.OptionCategoryResponse;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.entity.OptionCategory;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.menu.repository.OptionCategoryRepository;
import com.example.deliveryapp.domain.menu.repository.OptionItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuOptionOwnerService {

    private final MenuRepository menuRepository;
    private final OptionCategoryRepository optionCategoryRepository;
    private final OptionItemRepository optionItemRepository;

    public OptionCategoryResponse saveMenuOption(Long userId, Long menuId, OptionCategoryRequest request) {
        validateMenuOwner(userId, menuId);
        Menu menu = menuRepository.findActiveMenuByIdOrThrow(menuId);

        OptionCategory optionCategory = OptionCategoryConverter.toEntity(request, menu);
        optionCategoryRepository.save(optionCategory);

        return OptionCategoryConverter.toResponse(optionCategory);
    }

    public OptionCategoryResponse updateMenuOption(Long userId, Long menuId, Long optionCategoryId, OptionCategoryRequest request) {
        validateMenuOwner(userId, menuId);

        // 기존 OptionItem 삭제
        OptionCategory optionCategory = optionCategoryRepository.findByIdAndMenuIdOrThrow(optionCategoryId, menuId);
        optionCategory.update(request.getOptionCategoryName(), request.getIsRequired(), request.getIsMultiple(), request.getMaxOptions());
        optionCategory.clearOptionItems();

        // 새로운 OptionItem 저장
        request.getOptionItems()
                .stream().map(OptionCategoryConverter::toEntity)
                .forEach(optionCategory::addOptionItem);
        optionItemRepository.saveAll(optionCategory.getOptionItems());

        return OptionCategoryConverter.toResponse(optionCategory);
    }

    public void deleteMenuOption(Long userId, Long menuId, Long optionCategoryId) {
        validateMenuOwner(userId, menuId);

        OptionCategory optionCategory = optionCategoryRepository.findByIdAndMenuIdOrThrow(optionCategoryId, menuId);
        optionCategoryRepository.delete(optionCategory);
    }

    private void validateMenuOwner(Long userId, Long menuId) {
        Long ownerId = menuRepository.findOwnerIdByMenuIdOrThrow(menuId);
        if (!ownerId.equals(userId)) {
            throw new CustomException(ErrorCode.NOT_STORE_OWNER);
        }
    }
}
