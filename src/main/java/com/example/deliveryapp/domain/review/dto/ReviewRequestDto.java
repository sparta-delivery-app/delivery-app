package com.example.deliveryapp.domain.review.dto;

import lombok.Getter;

@Getter
public class ReviewRequestDto {
    private Long orderId;  // 주문 ID
    private String content;  // 리뷰 내용
    private Integer rating;  // 별점 (1~5)


}
