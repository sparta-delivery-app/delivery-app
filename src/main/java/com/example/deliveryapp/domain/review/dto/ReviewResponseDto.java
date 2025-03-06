package com.example.deliveryapp.domain.review.dto;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ReviewResponseDto {
    private String userName;  // 작성자 이름
    private Integer rating;   // 별점
    private String content;   // 리뷰 내용
    private String menuName;  // 주문한 메뉴 이름
    private LocalDateTime createdAt; // 리뷰 작성 시간

    public ReviewResponseDto(String userName, Integer rating, String content, String menuName, LocalDateTime createdAt) {
        this.userName = userName;
        this.rating = rating;
        this.content = content;
        this.menuName = menuName;
        this.createdAt = createdAt;
    }
}
