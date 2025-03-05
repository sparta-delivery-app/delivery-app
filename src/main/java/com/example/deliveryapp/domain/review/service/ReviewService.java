package com.example.deliveryapp.domain.review.service;

import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import com.example.deliveryapp.domain.order.repository.OrderMenuRepository;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.review.dto.ReviewRequestDto;
import com.example.deliveryapp.domain.review.dto.ReviewResponseDto;
import com.example.deliveryapp.domain.review.entity.Review;
import com.example.deliveryapp.domain.review.repository.ReviewRepository;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
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
    private final OrderMenuRepository orderMenuRepository;

    // 리뷰 생성
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto) {
        // 주문을 조회하여 배달 완료 상태인지 확인
        Order order = orderRepository.findById(reviewRequestDto.getOrderId())
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        if (!order.getOrderState().equals(OrderState.COMPLETED)) {
            throw new RuntimeException("배달 완료되지 않은 주문에 대해서는 리뷰를 작성할 수 없습니다.");
        }

        // 사용자 정보를 가져오기
        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 리뷰 객체 생성
        Review review = Review.builder()
                .user(user)
                .order(order)
                .content(reviewRequestDto.getContent())
                .rating(reviewRequestDto.getRating())
                .build();

        // 리뷰 저장
        Review savedReview = reviewRepository.save(review);

        // 메뉴 이름을 가져오기 위해 OrderMenuRepository에서 주문에 해당하는 메뉴 정보 가져오기
        String menuName = orderMenuRepository.findById(order.getId()).stream()
                .findFirst()
                .map(orderMenu -> orderMenu.getName())
                .orElse("메뉴 정보 없음");

        return new ReviewResponseDto(user.getName(), review.getRating(), review.getContent(), menuName, review.getCreatedAt());
    }

    // 리뷰 조회 (가게 기준, 최신순, 별점 범위 필터링, 페이징 처리)
    public Page<ReviewResponseDto> getReviewsByStoreId(Long storeId, Integer minRating, Integer maxRating, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Review> reviews = reviewRepository.findByOrderStoreIdAndRatingBetween(storeId, minRating, maxRating, pageRequest);

        return reviews.map(review -> {
            // 메뉴 이름을 가져오기 위해 OrderMenuRepository에서 주문에 해당하는 메뉴 정보 가져오기
            String menuName = orderMenuRepository.findById(review.getOrder().getId()).stream()
                    .findFirst()
                    .map(orderMenu -> orderMenu.getName())
                    .orElse("메뉴 정보 없음");
            return new ReviewResponseDto(review.getUser().getName(), review.getRating(), review.getContent(), menuName, review.getCreatedAt());
        });
    }

    // 내가 쓴 리뷰 전체 조회
    public List<ReviewResponseDto> getMyReviews(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(review -> {
                    String menuName = orderMenuRepository.findById(review.getOrder().getId()).stream()
                            .findFirst()
                            .map(orderMenu -> orderMenu.getName())
                            .orElse("메뉴 정보 없음");
                    return new ReviewResponseDto(review.getUser().getName(), review.getRating(), review.getContent(), menuName, review.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto reviewRequestDto) {
        // 리뷰를 ID로 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        // 리뷰 내용과 별점 수정
        review.setContent(reviewRequestDto.getContent());
        review.setRating(reviewRequestDto.getRating());

        // 수정된 리뷰 저장
        Review updatedReview = reviewRepository.save(review);

        // 수정된 리뷰를 ReviewResponseDto로 변환하여 반환
        String menuName = orderMenuRepository.findById(updatedReview.getOrder().getId()).stream()
                .findFirst()
                .map(orderMenu -> orderMenu.getName())
                .orElse("메뉴 정보 없음");

        return new ReviewResponseDto(updatedReview.getUser().getName(), updatedReview.getRating(), updatedReview.getContent(), menuName, updatedReview.getCreatedAt());
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        // 리뷰를 ID로 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        // 리뷰 삭제
        reviewRepository.delete(review);
    }
}
