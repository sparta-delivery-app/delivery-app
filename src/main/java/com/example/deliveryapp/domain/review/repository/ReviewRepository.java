package com.example.deliveryapp.domain.review.repository;

import com.example.deliveryapp.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);
    Page<Review> findByOrderStoreIdAndRatingBetween(Long storeId, Integer minRating, Integer maxRating, Pageable pageable);
}
