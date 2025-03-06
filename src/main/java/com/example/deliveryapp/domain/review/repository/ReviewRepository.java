package com.example.deliveryapp.domain.review.repository;

import com.example.deliveryapp.domain.review.entity.Review;
import com.example.deliveryapp.domain.store.dto.ReviewStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);
    Page<Review> findByOrderStoreIdAndRatingBetween(Long storeId, Integer minRating, Integer maxRating, Pageable pageable);

    @Query("SELECT new com.example.deliveryapp.domain.store.dto.ReviewStatistics(r.order.store.id, COUNT(r), AVG(r.rating)) " +
            "FROM Review r " +
            "WHERE r.order.store.id IN :storeIds " +
            "GROUP BY r.order.store.id")
    List<ReviewStatistics> countAndAverageRatingByStoreIds(@Param("storeIds") List<Long> storeIds);
}
