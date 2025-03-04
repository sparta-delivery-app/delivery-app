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
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class MenuOwnerServiceTest {

    @Mock
    private UserRepository userRepository;

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

        private MenuSaveRequest request;

        @BeforeEach
        void setUp() {
            request = new MenuSaveRequest("menu1", 15000L);
        }

        @Test
        @Order(1)
        void 메뉴저장_주인_권한_없음_실패() {
            // given
            User mockUser = mock(User.class);
            when(mockUser.getRole()).thenReturn(UserRole.USER);
            when(userRepository.findActiveUserByIdOrThrow(anyLong())).thenReturn(mockUser);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.saveMenu(1L, 1L, request)
            );

            assertEquals(ErrorCode.OWNER_ONLY_ACCESS, thrown.getErrorCode());
        }

        @Test
        @Order(2)
        void 메뉴저장_가게_주인_아님_실패() {
            // given
            User mockUser = mock(User.class);
            when(mockUser.getId()).thenReturn(1L);
            when(mockUser.getRole()).thenReturn(UserRole.OWNER);
            when(userRepository.findActiveUserByIdOrThrow(anyLong())).thenReturn(mockUser);

            Store mockStore = mock(Store.class);
            when(storeRepository.findActiveStoreByIdOrThrow(anyLong())).thenReturn(mockStore);

            User storeOwnerUser = mock(User.class);
            when(storeOwnerUser.getId()).thenReturn(2L);
            when(mockStore.getUser()).thenReturn(storeOwnerUser);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.saveMenu(mockUser.getId(), mockStore.getId(), request)
            );
            assertEquals(ErrorCode.NOT_STORE_OWNER, thrown.getErrorCode());

        }

        @Test
        @Order(3)
        void 메뉴저장_성공() {
            // given
            User mockUser = mock(User.class);
            when(mockUser.getId()).thenReturn(1L);
            when(mockUser.getRole()).thenReturn(UserRole.OWNER);
            when(userRepository.findActiveUserByIdOrThrow(anyLong())).thenReturn(mockUser);

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
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UpdateMenuTests {

        private MenuUpdateRequest request;

        @BeforeEach
        void setUp() {
            request = new MenuUpdateRequest("menu1", 15000L);
        }

        @Test
        @Order(1)
        void 메뉴수정_주인_권한_없음_실패() {
            // given
            User mockUser = mock(User.class);
            when(mockUser.getRole()).thenReturn(UserRole.USER);
            when(userRepository.findActiveUserByIdOrThrow(anyLong())).thenReturn(mockUser);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.updateMenu(1L, 1L, 1L, request)
            );

            assertEquals(ErrorCode.OWNER_ONLY_ACCESS, thrown.getErrorCode());
        }

        @Test
        @Order(2)
        void 메뉴수정_가게_주인_아님_실패() {
            // given
            User mockUser = mock(User.class);
            when(mockUser.getId()).thenReturn(1L);
            when(mockUser.getRole()).thenReturn(UserRole.OWNER);
            when(userRepository.findActiveUserByIdOrThrow(anyLong())).thenReturn(mockUser);

            Store mockStore = mock(Store.class);
            when(storeRepository.findActiveStoreByIdOrThrow(anyLong())).thenReturn(mockStore);

            User storeOwnerUser = mock(User.class);
            when(storeOwnerUser.getId()).thenReturn(2L);
            when(mockStore.getUser()).thenReturn(storeOwnerUser);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.updateMenu(mockUser.getId(), mockStore.getId(), 1L, request)
            );
            assertEquals(ErrorCode.NOT_STORE_OWNER, thrown.getErrorCode());
        }

        @Test
        @Order(3)
        void 메뉴수정_가게_다름_실패() {
            // given
            User mockUser = mock(User.class);
            when(mockUser.getId()).thenReturn(1L);
            when(mockUser.getRole()).thenReturn(UserRole.OWNER);
            when(userRepository.findActiveUserByIdOrThrow(anyLong())).thenReturn(mockUser);

            Store mockStore = mock(Store.class);
            when(mockStore.getId()).thenReturn(1L);
            when(mockStore.getUser()).thenReturn(mockUser);
            when(storeRepository.findActiveStoreByIdOrThrow(anyLong())).thenReturn(mockStore);

            Store anotherStore = mock(Store.class);
            when(anotherStore.getId()).thenReturn(2L);

            Menu mockMenu = mock(Menu.class);
            when(mockMenu.getStore()).thenReturn(anotherStore);
            when(menuRepository.findActiveMenuByIdOrThrow(anyLong())).thenReturn(mockMenu);

            // when & then
            CustomException thrown = assertThrows(CustomException.class,
                    () -> menuOwnerService.updateMenu(mockUser.getId(), mockStore.getId(), 1L, request)
            );
            assertEquals(ErrorCode.NOT_STORE_MENU, thrown.getErrorCode());
        }

        @Test
        @Order(4)
        void 메뉴수정_성공() {
            // given
            User mockUser = mock(User.class);
            when(mockUser.getId()).thenReturn(1L);
            when(mockUser.getRole()).thenReturn(UserRole.OWNER);
            when(userRepository.findActiveUserByIdOrThrow(anyLong())).thenReturn(mockUser);

            Store mockStore = mock(Store.class);
            when(mockStore.getId()).thenReturn(1L);
            when(mockStore.getUser()).thenReturn(mockUser);
            when(storeRepository.findActiveStoreByIdOrThrow(anyLong())).thenReturn(mockStore);

            Menu mockMenu = spy(Menu.class);
            when(mockMenu.getId()).thenReturn(1L);
            when(mockMenu.getStore()).thenReturn(mockStore);
            when(menuRepository.findActiveMenuByIdOrThrow(anyLong())).thenReturn(mockMenu);

            // when
            MenuResponse response = menuOwnerService.updateMenu(mockUser.getId(), mockStore.getId(), mockMenu.getId(), request);

            // then
            assertNotNull(response);
            assertEquals(mockMenu.getId(), response.getMenuId());
            assertEquals(request.getMenuName(), response.getMenuName());
            assertEquals(request.getPrice(), response.getPrice());
        }
    }
}