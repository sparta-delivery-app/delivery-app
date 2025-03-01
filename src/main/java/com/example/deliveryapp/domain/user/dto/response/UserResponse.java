package com.example.deliveryapp.domain.user.dto.response;

import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import lombok.Getter;

@Getter
public class UserResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final UserRole userRole;

    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.userRole = user.getRole();
    }
}
