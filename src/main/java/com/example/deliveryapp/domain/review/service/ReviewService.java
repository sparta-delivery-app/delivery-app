package com.example.deliveryapp.domain.review.service;

import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import com.example.deliveryapp.domain.review.dto.ReviewRequestDto;
import com.example.deliveryapp.domain.review.dto.ReviewResponseDto;
import com.example.deliveryapp.domain.review.entity.Review;
import com.example.deliveryapp.domain.review.repository.ReviewRepository;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // 리뷰 작성
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto) {
        // 주문을 조회하여 배달 완료 상태인지 확인
        Order order = orderRepository.findById(reviewRequestDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 배달 완료 상태가 아니면 예외 처리
        if (order.getOrderState() != OrderState.COMPLETED) {
            throw new RuntimeException("Order must be delivered before reviewing");
        }

        // 사용자 정보를 가져오기
        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Review 객체 생성
        Review review = Review.builder()
                .user(user)
                .order(order)
                .content(reviewRequestDto.getContent())
                .rating(reviewRequestDto.getRating())
                .build();

        // 리뷰 저장
        Review savedReview = reviewRepository.save(review);

        // 저장된 리뷰를 ReviewResponseDto로 변환
        return new ReviewResponseDto(savedReview);
    }

    // 가게 기준으로 리뷰 조회 (최신순 정렬 및 별점 범위 필터링)
    public List<ReviewResponseDto> getReviewsByStoreId(Long storeId, Integer minRating, Integer maxRating) {
        // 리뷰 조회
        List<Review> reviews = reviewRepository.findByOrderStoreId(storeId);

        // 별점 필터링
        if (minRating != null) {
            reviews = reviews.stream()
                    .filter(review -> review.getRating() >= minRating)
                    .collect(Collectors.toList());
        }
        if (maxRating != null) {
            reviews = reviews.stream()
                    .filter(review -> review.getRating() <= maxRating)
                    .collect(Collectors.toList());
        }

        // 최신순으로 정렬
        reviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));

        // ReviewResponseDto로 변환하여 반환
        return reviews.stream()
                .map(ReviewResponseDto::new)
                .collect(Collectors.toList());
    }
}
