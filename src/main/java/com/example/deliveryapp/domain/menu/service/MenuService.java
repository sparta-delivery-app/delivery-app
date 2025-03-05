package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.client.S3Service;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.dto.response.MenuPageResponse;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponseWithImageUrl;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuService {

    @Value("${s3.folder.menu}")
    private String MENU;

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public MenuPageResponse getMenus(Long storeId, Integer page, Integer size) {
        if (!storeRepository.existsByIdAndDeletedAtIsNull(storeId)) {
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("name").ascending());
        Page<MenuResponseWithImageUrl> menuPage = menuRepository.findAllByStoreIdAndDeletedAtIsNull(storeId, pageable)
                .map(menu -> MenuResponseWithImageUrl.builder()
                        .menuId(menu.getId())
                        .menuName(menu.getName())
                        .price(menu.getPrice())
                        .description(menu.getDescription())
                        .imageUrl(getImageUrl(menu.getImageUrl()))
                        .build());

        return new MenuPageResponse(menuPage);
    }

    private String getImageUrl(String originalImageUrl) {
        if (originalImageUrl == null) {
            return null;
        }

        return s3Service.createSignedUrl(MENU, originalImageUrl);
    }
}
