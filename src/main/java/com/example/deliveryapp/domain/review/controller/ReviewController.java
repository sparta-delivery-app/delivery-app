package com.example.deliveryapp.domain.review.controller;

import com.example.deliveryapp.domain.review.dto.ReviewRequestDto;
import com.example.deliveryapp.domain.review.dto.ReviewResponseDto;
import com.example.deliveryapp.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 내가 쓴 리뷰 전체 조회
    @GetMapping("/api/reviews/my-reviews/{userId}")
    public List<ReviewResponseDto> getMyReviews(@PathVariable Long userId) {
        return reviewService.getMyReviews(userId);
    }

    // 가게 기준으로 리뷰 조회
    @GetMapping("/api/reviews/store/{storeId}")
    public Page<ReviewResponseDto> getReviewsByStoreId(@PathVariable Long storeId,
                                                       @RequestParam(required = false) Integer minRating,
                                                       @RequestParam(required = false) Integer maxRating,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return reviewService.getReviewsByStoreId(storeId, minRating, maxRating, page, size);
    }

    // 리뷰 작성
    @PostMapping("/api/reviews/create")
    public ReviewResponseDto createReview(@RequestBody ReviewRequestDto reviewRequestDto) {
        return reviewService.createReview(reviewRequestDto);
    }

    // 리뷰 수정
    @PutMapping("/api/reviews/update/{reviewId}")
    public ReviewResponseDto updateReview(@PathVariable Long reviewId, @RequestBody ReviewRequestDto reviewRequestDto) {
        return reviewService.updateReview(reviewId, reviewRequestDto);
    }

    // 리뷰 삭제
    @DeleteMapping("/api/reviews/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return "리뷰가 삭제되었습니다.";
    }
}
