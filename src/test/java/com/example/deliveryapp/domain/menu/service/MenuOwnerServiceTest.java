package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.client.S3Service;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.dto.request.MenuRequest;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponse;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponseWithImageUrl;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import com.example.deliveryapp.domain.user.entity.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class MenuOwnerServiceTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private MenuOwnerService menuOwnerService;

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SaveMenuTests {

        private MenuRequest request;

        @BeforeEach
        void setUp() {
            request = new MenuRequest("menu1", 15000L, "description");
        }

        @Test
        @Order(1)
        void 메뉴저장_가게_주인_아님_실패() {
            // given
           Long userId = 1L;

            Store mockStore = mock(Store.class);
            when(storeRepository.findActiveStoreByIdOrThrow(anyLong())).thenReturn(mockStore);

            User storeOwnerUser = mock(User.class);
            when(storeOwnerUser.getId()).thenReturn(2L);
            when(mockStore.getUser()).thenReturn(storeOwnerUser);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.saveMenu(userId, mockStore.getId(), request)
            );
            assertEquals(ErrorCode.NOT_STORE_OWNER, thrown.getErrorCode());

        }

        @Test
        @Order(2)
        void 메뉴저장_성공() {
            // given
            User mockUser = mock(User.class);
            when(mockUser.getId()).thenReturn(1L);

            Store mockStore = mock(Store.class);
            when(mockStore.getUser()).thenReturn(mockUser);
            when(storeRepository.findActiveStoreByIdOrThrow(anyLong())).thenReturn(mockStore);

            // when
            MenuResponse response = menuOwnerService.saveMenu(mockUser.getId(), mockStore.getId(), request);

            // then
            verify(menuRepository, times(1)).save(any(Menu.class));

            assertNotNull(response);
            assertEquals(request.getMenuName(), response.getMenuName());
            assertEquals(request.getPrice(), response.getPrice());
            assertEquals(request.getDescription(), response.getDescription());
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UpdateMenuTests {

        private MenuRequest request;

        @BeforeEach
        void setUp() {
            request = new MenuRequest("menu1", 15000L, "description");
        }

        @Test
        @Order(1)
        void 메뉴수정_가게_주인_아님_실패() {
            // given
            Long userId = 1L;
            Long storeOwnerId = 2L;

            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(storeOwnerId);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.updateMenu(userId, 1L, 1L, request)
            );
            assertEquals(ErrorCode.NOT_STORE_OWNER, thrown.getErrorCode());
        }

        @Test
        @Order(2)
        void 메뉴수정_가게_다름_실패() {
            // given
            Long userId = 1L;
            Long storeId = 1L;

            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(userId);

            Store anotherStore = mock(Store.class);
            when(anotherStore.getId()).thenReturn(2L);

            Menu mockMenu = mock(Menu.class);
            when(mockMenu.getStore()).thenReturn(anotherStore);
            when(menuRepository.findActiveMenuByIdOrThrow(anyLong())).thenReturn(mockMenu);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.updateMenu(userId, storeId, 1L, request)
            );
            assertEquals(ErrorCode.NOT_STORE_MENU, thrown.getErrorCode());
        }

        @Test
        @Order(3)
        void 메뉴수정_성공() {
            // given
            Long userId = 1L;

            Store mockStore = mock(Store.class);
            when(mockStore.getId()).thenReturn(1L);
            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(userId);

            Menu mockMenu = spy(Menu.class);
            when(mockMenu.getId()).thenReturn(1L);
            when(mockMenu.getStore()).thenReturn(mockStore);
            when(menuRepository.findActiveMenuByIdOrThrow(anyLong())).thenReturn(mockMenu);

            // when
            MenuResponse response = menuOwnerService.updateMenu(userId, mockStore.getId(), mockMenu.getId(), request);

            // then
            assertNotNull(response);
            assertEquals(mockMenu.getId(), response.getMenuId());
            assertEquals(request.getMenuName(), response.getMenuName());
            assertEquals(request.getPrice(), response.getPrice());
            assertEquals(request.getDescription(), response.getDescription());
        }
    }

    @Nested
    @Order(3)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteMenuTests {

        @Test
        @Order(1)
        void 메뉴삭제_가게_주인_아님_실패() {
            // given
            Long userId = 1L;
            Long storeOwnerId = 2L;
            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(storeOwnerId);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.deleteMenu(userId, 1L, 1L)
            );
            assertEquals(ErrorCode.NOT_STORE_OWNER, thrown.getErrorCode());
        }

        @Test
        @Order(2)
        void 메뉴삭제_가게_다름_실패() {
            // given
            Long userId = 1L;
            Long storeId = 1L;

            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(userId);

            Store anotherStore = mock(Store.class);
            when(anotherStore.getId()).thenReturn(2L);

            Menu mockMenu = mock(Menu.class);
            when(mockMenu.getStore()).thenReturn(anotherStore);
            when(menuRepository.findActiveMenuByIdOrThrow(anyLong())).thenReturn(mockMenu);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.deleteMenu(userId, storeId, 1L)
            );
            assertEquals(ErrorCode.NOT_STORE_MENU, thrown.getErrorCode());
        }

        @Test
        @Order(3)
        void 메뉴삭제_성공() {
            // given
            Long userId = 1L;

            Store mockStore = mock(Store.class);
            when(mockStore.getId()).thenReturn(1L);
            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(userId);

            Menu mockMenu = mock(Menu.class);
            when(mockMenu.getStore()).thenReturn(mockStore);
            when(menuRepository.findActiveMenuByIdOrThrow(anyLong())).thenReturn(mockMenu);

            // when
            menuOwnerService.deleteMenu(userId, mockStore.getId(), 1L);

            // then
            verify(mockMenu, times(1)).setDeletedAt(any(LocalDateTime.class));
        }
    }

    @Nested
    @Order(4)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UploadMenuImageTests {

        @Test
        @Order(1)
        void 메뉴이미지업로드_가게_주인_아님_실패() {
            // given
            Long userId = 1L;
            Long storeOwnerId = 2L;
            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(storeOwnerId);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.uploadMenuImage(userId, 1L, 1L, null)
            );
            assertEquals(ErrorCode.NOT_STORE_OWNER, thrown.getErrorCode());
        }

        @Test
        @Order(2)
        void 메뉴이미지업로드_가게_다름_실패() {
            // given
            Long userId = 1L;
            Long storeId = 1L;

            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(userId);

            Store anotherStore = mock(Store.class);
            when(anotherStore.getId()).thenReturn(2L);

            Menu mockMenu = mock(Menu.class);
            when(mockMenu.getStore()).thenReturn(anotherStore);
            when(menuRepository.findActiveMenuByIdOrThrow(anyLong())).thenReturn(mockMenu);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.uploadMenuImage(userId, storeId, 1L, null)
            );
            assertEquals(ErrorCode.NOT_STORE_MENU, thrown.getErrorCode());
        }

        @Test
        @Order(3)
        void 메뉴이미지업로드_성공() {
            // given
            Long userId = 1L;
            String newImageUrl = "newImageUrl";
            String signedUrl = "signedUrl";

            Store mockStore = mock(Store.class);
            when(mockStore.getId()).thenReturn(1L);
            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(userId);

            Menu menu = Menu.builder().store(mockStore).build();
            menu.setImageUrl("unsignedUrl");
            when(menuRepository.findActiveMenuByIdOrThrow(anyLong())).thenReturn(menu);

            doNothing().when(s3Service).deleteImage(any(), anyString());
            when(s3Service.uploadImage(any(), any())).thenReturn(newImageUrl);
            when(s3Service.createSignedUrl(any(), eq(newImageUrl))).thenReturn(signedUrl);

            // when
            MenuResponseWithImageUrl response = menuOwnerService.uploadMenuImage(userId, mockStore.getId(), 1L, null);

            // then
            assertNotNull(response);
            assertEquals(signedUrl, response.getImageUrl());
        }
    }

    @Nested
    @Order(5)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteMenuImageTests {

        @Test
        @Order(1)
        void 메뉴이미지삭제_가게_주인_아님_실패() {
            // given
            Long userId = 1L;
            Long storeOwnerId = 2L;
            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(storeOwnerId);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.deleteMenuImage(userId, 1L, 1L)
            );
            assertEquals(ErrorCode.NOT_STORE_OWNER, thrown.getErrorCode());
        }

        @Test
        @Order(2)
        void 메뉴이미지삭제_가게_다름_실패() {
            // given
            Long userId = 1L;
            Long storeId = 1L;

            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(userId);

            Store anotherStore = mock(Store.class);
            when(anotherStore.getId()).thenReturn(2L);

            Menu mockMenu = mock(Menu.class);
            when(mockMenu.getStore()).thenReturn(anotherStore);
            when(menuRepository.findActiveMenuByIdOrThrow(anyLong())).thenReturn(mockMenu);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.deleteMenu(userId, storeId, 1L)
            );
            assertEquals(ErrorCode.NOT_STORE_MENU, thrown.getErrorCode());
        }

        @Test
        @Order(3)
        void 메뉴이미지삭제_성공() {
            // given
            Long userId = 1L;

            Store mockStore = mock(Store.class);
            when(mockStore.getId()).thenReturn(1L);
            when(storeRepository.findOwnerIdByStoreIdOrThrow(anyLong())).thenReturn(userId);

            Menu menu = Menu.builder().store(mockStore).build();
            menu.setImageUrl("unsignedUrl");
            when(menuRepository.findActiveMenuByIdOrThrow(anyLong())).thenReturn(menu);

            doNothing().when(s3Service).deleteImage(any(), anyString());

            // when
            menuOwnerService.deleteMenuImage(userId, mockStore.getId(), 1L);

            // then
            verify(s3Service, times(1)).deleteImage(any(), anyString());
            assertNull(menu.getImageUrl());
        }
    }
}