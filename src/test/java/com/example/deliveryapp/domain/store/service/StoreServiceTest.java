package com.example.deliveryapp.domain.store.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.store.dto.request.StoreSaveRequest;
import com.example.deliveryapp.domain.store.dto.response.StoreSaveResponse;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.enums.StoreStatus;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

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
        AuthUser authUser = new AuthUser(userId, "email", UserRole.OWNER);

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            storeService.save(User.fromAuthUser(authUser), request);
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
        AuthUser authUser = new AuthUser(userId, "email", UserRole.OWNER);
        User user = User.fromAuthUser(authUser);

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
}