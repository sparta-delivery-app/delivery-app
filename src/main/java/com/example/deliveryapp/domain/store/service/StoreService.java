package com.example.deliveryapp.domain.store.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.review.repository.ReviewRepository;
import com.example.deliveryapp.domain.store.dto.ReviewStatistics;
import com.example.deliveryapp.domain.store.dto.request.StoreSaveRequest;
import com.example.deliveryapp.domain.store.dto.request.StoreUpdateRequest;
import com.example.deliveryapp.domain.store.dto.response.StorePageResponse;
import com.example.deliveryapp.domain.store.dto.response.StoreResponse;
import com.example.deliveryapp.domain.store.dto.response.StoreSaveResponse;
import com.example.deliveryapp.domain.store.dto.response.StoreUpdateResponse;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.enums.StoreStatus;
import com.example.deliveryapp.domain.store.repository.StoreRepository;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final ReviewDeleteService reviewDeleteService;
    private final MenuRepository menuRepository;

    // 가게 생성
    @Transactional
    public StoreSaveResponse save(Long userId, StoreSaveRequest dto) {
        User user = userService.findById(userId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime openTime = LocalTime.parse(dto.getOpenTime(), formatter);
        LocalTime closeTime = LocalTime.parse(dto.getCloseTime(), formatter);
        StoreStatus status = StoreStatus.valueOf(dto.getStatus());
        Store store = new Store(dto.getName(), openTime, closeTime, dto.getMinimumOrderPrice(), status, user);
        storeRepository.save(store);
        return StoreSaveResponse.of(store, user);
    }

    // 가게 페이지 조회
    @Transactional(readOnly = true)
    public Page<StorePageResponse> findAllPage(Pageable pageable) {
        Page<Store> storePage = storeRepository.findAll(pageable);

        List<Long> storeIds = storePage.stream()
                .map(Store::getId)
                .collect(Collectors.toList());

        List<ReviewStatistics> reviewStatisticsList = reviewRepository.countAndAverageRatingByStoreIds(storeIds);
        Map<Long, ReviewStatistics> reviewStatisticsMap = reviewStatisticsList.stream()
                .collect(Collectors.toMap(ReviewStatistics::getStoreId, dto -> dto));

        return storePage.map(store -> {
            ReviewStatistics statisticsDto = reviewStatisticsMap.getOrDefault(store.getId(), new ReviewStatistics(store.getId(), 0L, 0.0));
            return StorePageResponse.of(
                    store,
                    statisticsDto.getAverageRating(),
                    statisticsDto.getCount()
            );
        });
    }

    // 가게 단건 조회
    @Transactional(readOnly = true)
    public StoreResponse findOne(Long id) {
        Store store = storeRepository.findById(id).
                orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        List<Menu> menus = menuRepository.findAllByStoreId(store.getId());
        return StoreResponse.of(store, menus.stream().map(MenuResponse::of).toList());
    }

    // 가게 수정
    @Transactional
    public StoreUpdateResponse update(Long storeId, Long userId, StoreUpdateRequest dto) {
        Store findStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        User findUser = userService.findById(userId);

        if (!userId.equals(findStore.getUser().getId())) {
            throw new CustomException(ErrorCode.INVALID_USER_UPDATE_STORE);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime openTime = LocalTime.parse(dto.getOpenTime(), formatter);
        LocalTime closeTime = LocalTime.parse(dto.getCloseTime(), formatter);
        StoreStatus status = StoreStatus.valueOf(dto.getStatus());

        findStore.update(dto.getName(), openTime, closeTime, dto.getMinimumOrderPrice(), status);
        return StoreUpdateResponse.of(findStore, findUser);
    }

    // 가게 삭제
    @Transactional
    public void delete(Long storeId, Long userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        if (!userId.equals(store.getUser().getId())) {
            throw new CustomException(ErrorCode.INVALID_USER_DELETE_STORE);
        }
        store.delete();
        reviewDeleteService.delete(storeId);
    }
}
