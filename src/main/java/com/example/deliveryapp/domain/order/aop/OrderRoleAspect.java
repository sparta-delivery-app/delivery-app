package com.example.deliveryapp.domain.order.aop;

import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.user.enums.UserRole;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OrderRoleAspect {

    @Before("execution(* com.example.deliveryapp.domain.order.controller.*UserController.*(..)) && args(authUser, ..)")
    public void checkUserRole(AuthUser authUser) {
        // USER 권한을 가진 사용자만 접근 가능
        if (!UserRole.USER.equals(authUser.getUserRole())) {
            throw new CustomException(ErrorCode.USER_ONLY_ACCESS);
        }
    }

    @Before("execution(* com.example.deliveryapp.domain.order.controller.OrderController.updateOrderState(..)) && args(authUser, ..)")
    public void checkOwnerRole(AuthUser authUser) {
        if (!UserRole.OWNER.equals(authUser.getUserRole())) {
            throw new CustomException(ErrorCode.OWNER_ONLY_ACCESS);
        }
    }
}
