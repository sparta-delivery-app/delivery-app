package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.dto.request.MenuSaveRequest;
import com.example.deliveryapp.domain.menu.dto.request.MenuUpdateRequest;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponse;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuOwnerService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    public MenuResponse saveMenu(Long userId, Long storeId, MenuSaveRequest request) {
        Store store = storeRepository.findActiveStoreByIdOrThrow(storeId);
        validateStoreOwner(store.getUser().getId(), userId);

        Menu menu = Menu.builder()
                .name(request.getMenuName())
                .price(request.getPrice())
                .store(store)
                .build();

        menuRepository.save(menu);

        return new MenuResponse(menu.getId(), menu.getName(), menu.getPrice());
    }

    public MenuResponse updateMenu(Long userId, Long storeId, Long menuId, MenuUpdateRequest request) {
        Long storeOwnerId = storeRepository.findOwnerIdByStoreIdOrThrow(storeId);
        validateStoreOwner(storeOwnerId, userId);

        Menu menu = menuRepository.findActiveMenuByIdOrThrow(menuId);
        validateMenuBelongsToStore(menu.getStore().getId(), storeId);

        menu.update(request.getMenuName(), request.getPrice());

        return new MenuResponse(menu.getId(), menu.getName(), menu.getPrice());
    }

    public void deleteMenu(Long userId, Long storeId, Long menuId) {
        Long storeOwnerId = storeRepository.findOwnerIdByStoreIdOrThrow(storeId);
        validateStoreOwner(storeOwnerId, userId);

        Menu menu = menuRepository.findActiveMenuByIdOrThrow(menuId);
        validateMenuBelongsToStore(menu.getStore().getId(), storeId);

        menuRepository.delete(menu);
    }

    private static void validateStoreOwner(Long storeOwnerId, Long currentUserId) {
        if (!storeOwnerId.equals(currentUserId)) {
            throw new CustomException(ErrorCode.NOT_STORE_OWNER);
        }
    }

    private static void validateMenuBelongsToStore(Long menuStoreId, Long storeId) {
        if (!menuStoreId.equals(storeId)) {
            throw new CustomException(ErrorCode.NOT_STORE_MENU);
        }
    }
}
