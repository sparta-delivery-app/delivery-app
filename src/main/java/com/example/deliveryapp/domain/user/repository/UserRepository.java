package com.example.deliveryapp.domain.user.repository;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNotNull(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndRole(Long userId, UserRole role);

    default User findByIdAndUserRoleOrThrow(Long id) {
        return findByIdAndRole(id, UserRole.USER)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
