package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.client.S3Service;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.dto.response.MenuPageResponse;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private MenuService menuService;

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetMenusTests {
        @Test
        @Order(1)
        void 메뉴조회_존재하지_않는_가게_실패() {
            // given
            given(storeRepository.existsByIdAndDeletedAtIsNull(anyLong())).willReturn(false);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuService.getMenus(1L, 1, 10)
            );
            assertEquals(ErrorCode.STORE_NOT_FOUND, thrown.getErrorCode());
        }

        @Test
        @Order(2)
        void 메뉴조회_성공() {
            // given
            Integer page = 1, size = 10;
            List<Menu> menuList = List.of( mock(Menu.class), mock(Menu.class));
            when(menuList.get(0).getImageUrl()).thenReturn("unsignedUrl");
            String signedUrl = "signedUrl";

            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("name").ascending());
            Page<Menu> menuPage = new PageImpl<>(menuList, pageable, menuList.size());

            given(storeRepository.existsByIdAndDeletedAtIsNull(anyLong())).willReturn(true);
            given(menuRepository.findAllByStoreIdAndDeletedAtIsNull(anyLong(), any(Pageable.class))).willReturn(menuPage);
            given(s3Service.createSignedUrl(any(), anyString())).willReturn(signedUrl);

            // when
            MenuPageResponse response = menuService.getMenus(1L, page, size);

            // then
            assertNotNull(response);
            assertEquals(menuList.size(), response.getContent().size());
            assertEquals(menuList.size(), response.getTotalElements());
            assertEquals(1, response.getTotalPages());
            assertEquals(page, response.getCurrentPage());
            assertEquals(size, response.getSize());
            assertEquals(signedUrl, response.getContent().get(0).getImageUrl());
        }
    }
}