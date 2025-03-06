package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.domain.menu.dto.response.OptionCategoryPageResponse;
import com.example.deliveryapp.domain.menu.entity.OptionCategory;
import com.example.deliveryapp.domain.menu.repository.OptionCategoryRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class MenuOptionServiceTest {

    @Mock
    private OptionCategoryRepository optionCategoryRepository;

    @InjectMocks
    private MenuOptionService menuOptionService;

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetMenuOptionsTests {
        @Test
        @Order(1)
        void 메뉴_옵션_조회_성공() {
            // given
            int page = 1, size = 10;
            Pageable pageable = PageRequest.of(page - 1, size);

            List<OptionCategory> optionCategoryList = List.of(
                    mock(OptionCategory.class),
                    mock(OptionCategory.class)
            );
            Page<OptionCategory> optionCategoryPage = new PageImpl<>(optionCategoryList, pageable, optionCategoryList.size());

            given(optionCategoryRepository.findAllByMenuId(anyLong(), any(Pageable.class))).willReturn(optionCategoryPage);

            // when
            OptionCategoryPageResponse response = menuOptionService.getMenuOptions(1L, page, size);

            // then
            assertNotNull(response);
            assertEquals(optionCategoryList.size(), response.getContent().size());
            assertEquals(optionCategoryList.size(), response.getTotalElements());
            assertEquals(1, response.getTotalPages());
            assertEquals(page, response.getCurrentPage());
            assertEquals(size, response.getSize());
        }
    }
}