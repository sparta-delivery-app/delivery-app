package com.example.deliveryapp.domain.review.repository;

import com.example.deliveryapp.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // storeId와 별점 범위로 리뷰 조회 (최신순으로 정렬)
    List<Review> findByOrder_Store_IdAndRatingBetweenOrderByIdDesc(Long storeId, int minRating, int maxRating);

    List<Review> findByOrder_Store_Id(Long storeId);

    List<Review> findByOrderStoreId(Long storeId);

    List<Review> findByUserId(Long userid);
}
