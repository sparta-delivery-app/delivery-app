package com.example.deliveryapp.domain.common.dto;

import com.example.deliveryapp.domain.user.enums.UserRole;
import lombok.Getter;

@Getter
public class AuthUser {
    private final Long id;
    private final String email;
    private final String name;
    private final UserRole userRole;

    public AuthUser(Long id, String email, String name, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.userRole = userRole;
    }
}
