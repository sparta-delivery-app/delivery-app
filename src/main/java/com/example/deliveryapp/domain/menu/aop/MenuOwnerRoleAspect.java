package com.example.deliveryapp.domain.menu.aop;

import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.user.enums.UserRole;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MenuOwnerRoleAspect {

    @Before("within(com.example.deliveryapp.domain.menu.controller.*OwnerController) && " +
            "args(authUser, ..)")
    public void checkOwnerRole(AuthUser authUser) {
        if (!UserRole.OWNER.equals(authUser.getUserRole())) {
            throw new CustomException(ErrorCode.OWNER_ONLY_ACCESS);
        }
    }
}
