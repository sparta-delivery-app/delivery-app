package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.client.S3Service;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.dto.request.MenuSaveRequest;
import com.example.deliveryapp.domain.menu.dto.request.MenuUpdateRequest;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponse;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponseWithImageUrl;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuOwnerService {

    @Value("${s3.folder.menu}")
    private String MENU;

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final S3Service s3Service;

    public MenuResponse saveMenu(Long userId, Long storeId, MenuSaveRequest request) {
        Store store = storeRepository.findActiveStoreByIdOrThrow(storeId);
        validateStoreOwner(store.getUser().getId(), userId);

        Menu menu = Menu.builder()
                .name(request.getMenuName())
                .price(request.getPrice())
                .description(request.getDescription())
                .store(store)
                .build();

        menuRepository.save(menu);

        return MenuResponse.builder()
                .menuId(menu.getId())
                .menuName(menu.getName())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .build();
    }

    public MenuResponse updateMenu(Long userId, Long storeId, Long menuId, MenuUpdateRequest request) {
        Long storeOwnerId = storeRepository.findOwnerIdByStoreIdOrThrow(storeId);
        validateStoreOwner(storeOwnerId, userId);

        Menu menu = menuRepository.findActiveMenuByIdOrThrow(menuId);
        validateMenuBelongsToStore(menu.getStore().getId(), storeId);

        menu.update(request.getMenuName(), request.getPrice(), request.getDescription());

        return MenuResponse.builder()
                .menuId(menu.getId())
                .menuName(menu.getName())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .build();
    }

    public void deleteMenu(Long userId, Long storeId, Long menuId) {
        Long storeOwnerId = storeRepository.findOwnerIdByStoreIdOrThrow(storeId);
        validateStoreOwner(storeOwnerId, userId);

        Menu menu = menuRepository.findActiveMenuByIdOrThrow(menuId);
        validateMenuBelongsToStore(menu.getStore().getId(), storeId);

        menu.setDeletedAt(LocalDateTime.now());
    }

    public MenuResponseWithImageUrl uploadMenuImage(Long userId, Long storeId, Long menuId, MultipartFile file) {
        Long storeOwnerId = storeRepository.findOwnerIdByStoreIdOrThrow(storeId);
        validateStoreOwner(storeOwnerId, userId);

        Menu menu = menuRepository.findActiveMenuByIdOrThrow(menuId);
        validateMenuBelongsToStore(menu.getStore().getId(), storeId);

        // 기존 이미지 삭제
        if (menu.getImageUrl() != null) {
            s3Service.deleteImage(MENU, menu.getImageUrl());
        }

        String imageUrl = s3Service.uploadImage(MENU, file);
        menu.setImageUrl(imageUrl);

        return MenuResponseWithImageUrl.builder()
                .menuId(menu.getId())
                .menuName(menu.getName())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .imageUrl(s3Service.createSignedUrl(MENU, menu.getImageUrl()))
                .build();
    }

    public void deleteMenuImage(Long userId, Long storeId, Long menuId) {
        Long storeOwnerId = storeRepository.findOwnerIdByStoreIdOrThrow(storeId);
        validateStoreOwner(storeOwnerId, userId);

        Menu menu = menuRepository.findActiveMenuByIdOrThrow(menuId);
        validateMenuBelongsToStore(menu.getStore().getId(), storeId);

        if (menu.getImageUrl() != null) {
            s3Service.deleteImage(MENU, menu.getImageUrl());
            menu.setImageUrl(null);
        }
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
