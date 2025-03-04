package com.example.deliveryapp.domain.store.service;

import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.review.repository.ReviewRepository;
import com.example.deliveryapp.domain.store.dto.request.StoreSaveRequest;
import com.example.deliveryapp.domain.store.dto.response.StorePageResponse;
import com.example.deliveryapp.domain.store.dto.response.StoreSaveResponse;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.enums.StoreStatus;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import com.example.deliveryapp.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private StoreService storeService;

    @Test
    public void 가게_등록_중_사장을_찾지_못해_에러가_발생한다() {
        // given
        long userId = 1L;
        StoreSaveRequest request = new StoreSaveRequest(
                "가게 이름",
                "10:00",
                "20:00",
                10000L,
                "OPEN"
        );
        AuthUser authUser = new AuthUser(userId, "email", "name", UserRole.OWNER);
        User user = new User(String.valueOf(authUser.getId()), authUser.getEmail(), authUser.getName(), authUser.getUserRole());
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            storeService.save(user.getId(), request);
        });

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void 가게를_정상적으로_등록한다() {
        // given
        long userId = 1L;
        StoreSaveRequest request = new StoreSaveRequest(
                "가게 이름",
                "10:00",
                "20:00",
                10000L,
                "OPEN"
        );
        AuthUser authUser = new AuthUser(userId, "email", "name", UserRole.OWNER);
        User user = new User(String.valueOf(authUser.getId()), authUser.getEmail(), authUser.getName(), authUser.getUserRole());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime openTime = LocalTime.parse(request.getOpenTime(), formatter);
        LocalTime closeTime = LocalTime.parse(request.getCloseTime(), formatter);
        StoreStatus status = StoreStatus.valueOf(request.getStatus());

        Store store = new Store(request.getName(), openTime, closeTime, request.getMinimumOrderPrice(), status, user);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(storeRepository.save(ArgumentMatchers.any(Store.class))).willReturn(store);

        // when
        StoreSaveResponse result = storeService.save(authUser.getId(), request);

        // then
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getMinimumOrderPrice(), result.getMinimumOrderPrice());
    }

    @Test
    public void 가게_단건_조회_중_가게를_찾지_못해_에러가_발생한다() {
        // given
        long storeId = 1L;
        given(storeRepository.findById(storeId)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            storeService.findOne(storeId);
        });

        // then
        assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void 가게_페이지를_정상_조회한다() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        List<Store> stores = new ArrayList<>();
        User user = User.builder()
                .email("test@test.com")
                .password("password")
                .name("testName")
                .role(UserRole.USER).
                build();

        stores.add(new Store("가게1", LocalTime.of(10, 0), LocalTime.of(20,0), 10000L, StoreStatus.OPEN, user));
        Page<Store> storesPage = new PageImpl<>(stores, pageable, stores.size());
        given(storeRepository.findAll(pageable)).willReturn(storesPage);
        given(reviewRepository.countAndAverageRatingByStoreIds(anyList()).willReturn(new ArrayList<>());

        // when
        Page<StorePageResponse> result = storeService.findAllPage(pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("가게1", result.getContent().get(0).getName());
    }
}