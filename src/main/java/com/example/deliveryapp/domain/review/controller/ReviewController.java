package com.example.deliveryapp.domain.review.controller;

import com.example.deliveryapp.domain.review.dto.ReviewRequestDto;
import com.example.deliveryapp.domain.review.dto.ReviewResponseDto;
import com.example.deliveryapp.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
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
    public List<ReviewResponseDto> getReviewsByStoreId(@PathVariable Long storeId,
                                                       @RequestParam(required = false) Integer minRating,
                                                       @RequestParam(required = false) Integer maxRating) {
        return reviewService.getReviewsByStoreId(storeId, minRating, maxRating);
    }

    // 리뷰 작성
    @PostMapping("/api/reviews/create")
    public ReviewResponseDto createReview(@RequestBody ReviewRequestDto reviewRequestDto) {
        return reviewService.createReview(reviewRequestDto);
    }
}

