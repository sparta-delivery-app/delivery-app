package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponse;
import com.example.deliveryapp.domain.menu.dto.response.MenuPageResponse;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public MenuPageResponse getMenus(Long storeId, Integer page, Integer size) {
        if (!storeRepository.existsByIdAndDeletedAtIsNull(storeId)) {
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("name").ascending());
        Page<MenuResponse> menuPage = menuRepository.findAllByStoreId(storeId, pageable);
        return new MenuPageResponse(menuPage);
    }
}
