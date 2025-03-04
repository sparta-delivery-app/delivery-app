package com.example.deliveryapp.domain.user.repository;

import com.example.deliveryapp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNotNull(String email);

    Optional<User> findByEmail(String email);
}
