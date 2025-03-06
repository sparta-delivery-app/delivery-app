package com.example.deliveryapp.domain.store.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.dto.response.MenuResponse;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
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
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // 가게 생성
    @Transactional
    public StoreSaveResponse save(Long userId, StoreSaveRequest dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.OWNER) {
            throw new CustomException(ErrorCode.INVALID_USER_ROLE);
        }

        if (storeRepository.countByUserIdAndIsDeletedFalse(userId) >= 4) {
            throw new CustomException(ErrorCode.STORE_LIMIT_EXCEEDED);
        }

        LocalTime openTime = LocalTime.parse(dto.getOpenTime(), FORMATTER);
        LocalTime closeTime = LocalTime.parse(dto.getCloseTime(), FORMATTER);
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

        if (store.getStatus() == StoreStatus.PERMANENTLY_CLOSED) {
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }

        List<MenuResponse> menus = menuRepository.findListByStoreId(store.getId());
        return StoreResponse.of(store, menus);
    }

    // 가게 수정
    @Transactional
    public StoreUpdateResponse update(Long storeId, Long userId, StoreUpdateRequest dto) {
        Store findStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        User findUser = userRepository.findById(userId).
                orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!userId.equals(findStore.getUser().getId())) {
            throw new CustomException(ErrorCode.INVALID_USER_UPDATE_STORE);
        }

        if (dto.getStatus() != null && dto.getStatus().equals(StoreStatus.PERMANENTLY_CLOSED.name())) {
            throw new CustomException(ErrorCode.STORE_STATUS_CANNOT_BE_CHANGED_TO_CLOSED);
        }

        LocalTime openTime = LocalTime.parse(dto.getOpenTime(), FORMATTER);
        LocalTime closeTime = LocalTime.parse(dto.getCloseTime(), FORMATTER);
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

        if (store.getStatus() == StoreStatus.PERMANENTLY_CLOSED) {
            throw new CustomException(ErrorCode.STORE_ALREADY_CLOSED);
        }

        if (orderRepository.existsByStoreId(storeId)) {
            throw new CustomException(ErrorCode.STORE_HAS_ORDERS);
        }

        store.closeStore();
        store.setDeletedAt(LocalDateTime.now());
        storeRepository.save(store);

    }

}
