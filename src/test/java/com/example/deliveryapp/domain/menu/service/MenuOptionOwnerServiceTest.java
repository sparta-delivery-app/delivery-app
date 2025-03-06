package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.dto.request.OptionCategoryRequest;
import com.example.deliveryapp.domain.menu.dto.request.OptionItemRequest;
import com.example.deliveryapp.domain.menu.dto.response.OptionCategoryResponse;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.entity.OptionCategory;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.menu.repository.OptionCategoryRepository;
import com.example.deliveryapp.domain.menu.repository.OptionItemRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class MenuOptionOwnerServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private OptionCategoryRepository optionCategoryRepository;

    @Mock
    private OptionItemRepository optionItemRepository;

    @InjectMocks
    private MenuOptionOwnerService menuOptionOwnerService;

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SaveMenuOptionTests {

        private OptionCategoryRequest request;

        @BeforeEach
        void setUp() {
            List<OptionItemRequest> optionItemRequests = List.of(
                    new OptionItemRequest("item1", 1000L),
                    new OptionItemRequest("item2", 2000L)
            );
            request = new OptionCategoryRequest(
                    "category1",
                    false,
                    true,
                    null,
                    optionItemRequests
            );
        }

        @Test
        @Order(1)
        void 메뉴_옵션_저장_메뉴_주인_아님_실패() {
            // given
            Long userId = 1L;
            Long menuOwnerId = 2L;

            given(menuRepository.findOwnerIdByMenuIdOrThrow(anyLong())).willReturn(menuOwnerId);

            // when & then
            CustomException customException = assertThrows(CustomException.class,
                    () -> menuOptionOwnerService.saveMenuOption(userId, 1L, request)
            );
            assertEquals(ErrorCode.NOT_STORE_OWNER, customException.getErrorCode());
        }

        @Test
        @Order(2)
        void 메뉴_옵션_저장_성공() {
            // given
            Long userId = 1L;
            given(menuRepository.findOwnerIdByMenuIdOrThrow(anyLong())).willReturn(userId);

            Menu menu = mock(Menu.class);
            given(menuRepository.findActiveMenuByIdOrThrow(anyLong())).willReturn(menu);

            // when
            OptionCategoryResponse response = menuOptionOwnerService.saveMenuOption(userId, 1L, request);

            // then
            assertNotNull(response);
            assertEquals(request.getOptionCategoryName(), response.getOptionCategoryName());
            assertEquals(request.getIsRequired(), response.getIsRequired());
            assertEquals(request.getIsMultiple(), response.getIsMultiple());
            assertEquals(request.getMaxOptions(), response.getMaxOptions());
            assertEquals(request.getOptionItems().size(), response.getOptionItems().size());

            verify(optionCategoryRepository, times(1)).save(any(OptionCategory.class));
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UpdateMenuOptionTests {

        private OptionCategoryRequest request;

        @BeforeEach
        void setUp() {
            List<OptionItemRequest> optionItemRequests = List.of(
                    new OptionItemRequest("item1", 1000L),
                    new OptionItemRequest("item2", 2000L)
            );
            request = new OptionCategoryRequest(
                    "category1",
                    false,
                    true,
                    null,
                    optionItemRequests
            );
        }

        @Test
        @Order(1)
        void 메뉴_옵션_수정_메뉴_주인_아님_실패() {
            // given
            Long userId = 1L;
            Long menuOwnerId = 2L;

            given(menuRepository.findOwnerIdByMenuIdOrThrow(anyLong())).willReturn(menuOwnerId);

            // when & then
            CustomException customException = assertThrows(CustomException.class,
                    () -> menuOptionOwnerService.updateMenuOption(userId, 1L, 1L, request)
            );
            assertEquals(ErrorCode.NOT_STORE_OWNER, customException.getErrorCode());
        }

        @Test
        @Order(2)
        void 메뉴_옵션_수정_성공() {
            // given
            Long userId = 1L;
            given(menuRepository.findOwnerIdByMenuIdOrThrow(anyLong())).willReturn(userId);

            OptionCategory mockOptionCategory = spy(OptionCategory.class);
            given(mockOptionCategory.getId()).willReturn(1L);
            given(optionCategoryRepository.findByIdAndMenuIdOrThrow(anyLong(), anyLong())).willReturn(mockOptionCategory);


            // when
            OptionCategoryResponse response = menuOptionOwnerService.updateMenuOption(userId, 1L, 1L, request);

            // then
            assertNotNull(response);
            assertEquals(mockOptionCategory.getId(), response.getOptionCategoryId());
            assertEquals(request.getOptionCategoryName(), response.getOptionCategoryName());
            assertEquals(request.getIsRequired(), response.getIsRequired());
            assertEquals(request.getIsMultiple(), response.getIsMultiple());
            assertEquals(request.getMaxOptions(), response.getMaxOptions());
            assertEquals(request.getOptionItems().size(), response.getOptionItems().size());

            verify(optionItemRepository, times(1)).saveAll(anyList());
        }
    }

    @Nested
    @Order(3)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteMenuOptionTests {

        @Test
        @Order(1)
        void 메뉴_옵션_삭제_메뉴_주인_아님_실패() {
            // given
            Long userId = 1L;
            Long menuOwnerId = 2L;

            given(menuRepository.findOwnerIdByMenuIdOrThrow(anyLong())).willReturn(menuOwnerId);

            // when & then
            CustomException customException = assertThrows(CustomException.class,
                    () -> menuOptionOwnerService.deleteMenuOption(userId, 1L, 1L)
            );
            assertEquals(ErrorCode.NOT_STORE_OWNER, customException.getErrorCode());
        }

        @Test
        @Order(2)
        void 메뉴_옵션_삭제_성공() {
            // given
            Long userId = 1L;
            given(menuRepository.findOwnerIdByMenuIdOrThrow(anyLong())).willReturn(userId);

            OptionCategory mockOptionCategory = mock(OptionCategory.class);
            given(optionCategoryRepository.findByIdAndMenuIdOrThrow(anyLong(), anyLong())).willReturn(mockOptionCategory);
            doNothing().when(optionCategoryRepository).delete(any(OptionCategory.class));

            // when
            menuOptionOwnerService.deleteMenuOption(userId, 1L, 1L);

            // then
            verify(optionCategoryRepository, times(1)).delete(any(OptionCategory.class));
        }
    }
}