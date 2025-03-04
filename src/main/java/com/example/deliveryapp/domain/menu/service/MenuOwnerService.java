package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.dto.request.MenuSaveRequest;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponse;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuOwnerService {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    public MenuResponse saveMenu(Long userId, Long storeId, MenuSaveRequest request) {
        User user = userRepository.findActiveUserByIdOrThrow(userId);
        validateOwnerRole(user.getRole());

        Store store = storeRepository.findActiveStoreByIdOrThrow(storeId);
        validateStoreOwner(store.getUser(), user);

        Menu menu = Menu.builder()
                .name(request.getMenuName())
                .price(request.getPrice())
                .store(store)
                .build();

        menuRepository.save(menu);

        return new MenuResponse(menu.getId(), menu.getName(), menu.getPrice());
    }

    private static void validateOwnerRole(UserRole userRole) {
        if (!UserRole.OWNER.equals(userRole)) {
            throw new CustomException(ErrorCode.OWNER_ONLY_ACCESS);
        }
    }

    private static void validateStoreOwner(User storeOwner, User currentUser) {
        if (!storeOwner.getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.NOT_STORE_OWNER);
        }
    }
}
