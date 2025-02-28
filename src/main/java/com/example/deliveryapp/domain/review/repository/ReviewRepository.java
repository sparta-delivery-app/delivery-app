package com.example.deliveryapp.domain.review.repository;

import com.example.deliveryapp.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
