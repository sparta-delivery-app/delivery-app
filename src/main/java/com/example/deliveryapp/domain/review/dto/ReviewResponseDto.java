package com.example.deliveryapp.domain.review.dto;

import com.example.deliveryapp.domain.review.entity.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponseDto {
    private String userName; // 작성자 이름
    private Integer rating;  // 별점
    private String content;  // 리뷰 내용
    private String menuName; // 주문 메뉴 이름
    private LocalDateTime createdAt; // 리뷰 작성 시간

    public ReviewResponseDto(Review review) {
        this.userName = review.getUser().getName(); // 사용자 이름
        this.rating = review.getRating();  // 별점
        this.content = review.getContent();  // 리뷰 내용
        this.menuName = review.getOrder().getOrderMenu().getName(); // 주문 메뉴 이름 (Order 엔티티에서 메뉴 이름이 있다고 가정)
        this.createdAt = review.getCreatedAt(); // 리뷰 작성 시간
    }
}
