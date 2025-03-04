package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.dto.response.MenuPageResponse;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private StoreRepository storeRepository;

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
            List<MenuResponse> menuResponseList = List.of(
                    new MenuResponse(1L, "menu1", 5000L),
                    new MenuResponse(2L, "menu2", 15000L)
            );
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("name").ascending());
            Page<MenuResponse> menuPage = new PageImpl<>(menuResponseList, pageable, menuResponseList.size());

            given(storeRepository.existsByIdAndDeletedAtIsNull(anyLong())).willReturn(true);
            given(menuRepository.findAllByStoreId(anyLong(), any(Pageable.class))).willReturn(menuPage);

            // when & then
            MenuPageResponse response = menuService.getMenus(1L, page, size);
            assertNotNull(response);
            assertEquals(menuResponseList.size(), response.getContent().size());
            assertEquals(menuResponseList.size(), response.getTotalElements());
            assertEquals(1, response.getTotalPages());
            assertEquals(page, response.getCurrentPage());
            assertEquals(size, response.getSize());
        }
    }
}