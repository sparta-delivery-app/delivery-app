package com.example.deliveryapp.domain.store.service;

import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import com.example.deliveryapp.domain.review.repository.ReviewRepository;
import com.example.deliveryapp.domain.store.dto.request.StoreSaveRequest;
import com.example.deliveryapp.domain.store.dto.request.StoreUpdateRequest;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.annotations.SdkTestInternalApi;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private StoreService storeService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Test
    void 가게를_정상적으로_등록한다() {
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
        given(storeRepository.save(any(Store.class))).willReturn(store);

        // when
        StoreSaveResponse result = storeService.save(authUser.getId(), request);

        // then
        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getMinimumOrderPrice(), result.getMinimumOrderPrice());
    }

    @Test
    void 가게_등록_중_사장을_찾지_못해_에러가_발생한다() {
        // given
        long userId = 1L;
        StoreSaveRequest request = new StoreSaveRequest(
                "가게 이름",
                "10:00",
                "20:00",
                10000L,
                "OPEN"
        );
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            storeService.save(userId, request);
        });

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 가게_단건_조회_중_가게를_찾지_못해_에러가_발생한다() {
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
    void 가게_수정_중_가게를_찾지_못해_에러가_발생한다() {
        // given
        long storeId = 1L;
        long userId = 1L;
        StoreUpdateRequest request = new StoreUpdateRequest("가게1", "10:00", "20:00", 1000L, "OPEN");
        given(storeRepository.findById(storeId)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> storeService.update(storeId, userId, request));

        // then
        assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 가게_수정_중_사장_ID가_다른_경우_에러가_발생한다() {
        // given
        long storeId = 1L;
        long userId = 1L;
        long otherUserId = 2L;
        StoreUpdateRequest request = new StoreUpdateRequest("가게1", "10:00", "20:00", 1000L, "OPEN");
        User user = User.builder()
                .email("email")
                .password("password")
                .name("name")
                .role(UserRole.OWNER)
                .build();

        User otherUser = User.builder()
                .email("otherEmail")
                .password("otherPassword")
                .name("otherName")
                .role(UserRole.OWNER)
                .build();

        Store store = new Store("가게1", LocalTime.of(10, 0), LocalTime.of(20, 0), 100L, StoreStatus.OPEN, user);
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(userRepository.findById(otherUserId)).willReturn(Optional.of(otherUser));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> storeService.update(storeId, otherUserId, request));

        // then
        assertEquals(ErrorCode.INVALID_USER_UPDATE_STORE, exception.getErrorCode());
    }

    @Test
    void 가게_수정_중_상태를_폐업으로_변하려는_경우_에러가_발생한다() {
        //given
        long storeId = 1L;
        long userId = 1L;
        StoreUpdateRequest request = new StoreUpdateRequest("가게1", "10:00", "20:00", 1000L, "PERMANENTLY_CLOSED");
        User user = User.builder()
                .email("email")
                .password("password")
                .name("name")
                .role(UserRole.OWNER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        Store store = new Store("가게1", LocalTime.of(10, 0), LocalTime.of(20, 0), 100L, StoreStatus.OPEN, user);
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> storeService.update(storeId, userId, request));

        // then
        assertEquals(ErrorCode.STORE_STATUS_CANNOT_BE_CHANGED_TO_CLOSED, exception.getErrorCode());
    }

    @Test
    void 가게_수정_중_상태를_영업_종료로_변경하려는_경우_성공한다() {
        //given
        long storeId = 1L;
        long userId = 1L;
        StoreUpdateRequest request = new StoreUpdateRequest("가게1", "10:00", "20:00", 1000L, "CLOSED_BY_TIME");
        User user = User.builder()
                .email("email")
                .password("password")
                .name("name")
                .role(UserRole.OWNER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        Store store = new Store("가게1", LocalTime.of(10, 0), LocalTime.of(20, 0), 100L, StoreStatus.OPEN, user);
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        storeService.update(storeId, userId, request);

        // then
        verify(storeRepository).findById(storeId);
        verify(userRepository).findById(userId);

        assertEquals(StoreStatus.CLOSED_BY_TIME, store.getStatus());
    }

    @Test
    void 가게_삭제_중_가게를_찾지_못해_에러가_발생한다() {
        // given
        long storeId = 1L;
        long userId = 1L;
        given(storeRepository.findById(storeId)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> storeService.delete(storeId, userId));

        // then
        assertEquals(ErrorCode.STORE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 가게_삭제_중_사장_ID가_다른_경우_에러가_발생한다() {
        // given
        long storeId = 1L;
        long userId = 1L;
        long otherUserId = 2L;
        User user = User.builder()
                .email("email")
                .password("password")
                .name("name")
                .role(UserRole.OWNER)
                .build();
        Store store = new Store("가게1", LocalTime.of(10, 0), LocalTime.of(20, 0), 10000L, StoreStatus.OPEN, user);
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> storeService.delete(storeId, otherUserId));

        // then
        assertEquals(ErrorCode.INVALID_USER_DELETE_STORE, exception.getErrorCode());
    }

//    @Test
//    void 가게_페이지를_정상_조회한다() {
//        // given
//        PageRequest pageable = PageRequest.of(0, 10);
//        List<Store> stores = new ArrayList<>();
//        User user = User.builder()
//                .email("test@test.com")
//                .password("password")
//                .name("testName")
//                .role(UserRole.USER).
//                build();
//
//        stores.add(new Store("가게1", LocalTime.of(10, 0), LocalTime.of(20,0), 10000L, StoreStatus.OPEN, user));
//        Page<Store> storesPage = new PageImpl<>(stores, pageable, stores.size());
//        given(storeRepository.findAll(pageable)).willReturn(storesPage);
//        //given(reviewRepository.countAndAverageRatingByStoreIds(anyList()).willReturn(new ArrayList<>());
//
//        // when
//        //Page<StorePageResponse> result = storeService.findAllPage(pageable);
//
//        // then
////        assertNotNull(result);
////        assertEquals(1, result.getContent().size());
////        assertEquals("가게1", result.getContent().get(0).getName());
//    }
}