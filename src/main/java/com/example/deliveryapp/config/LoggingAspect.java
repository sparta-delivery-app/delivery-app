package com.example.deliveryapp.config;

import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final OrderRepository orderRepository;

    @Pointcut("execution(* com.example.deliveryapp.domain.order.controller.OrderController.createOrder(..))")
    public void createOrderMethod() {
    }

    @Pointcut("execution(* com.example.deliveryapp.domain.order.controller.OrderController.updateOrderState(..))")
    public void updateOrderStateMethod() {
    }

    // 주문 생성 메서드
    @AfterReturning(value = "createOrderMethod()", returning = "orderResponse")
    public void logCreateOrderAction(OrderResponse orderResponse) {
        logOrderAction("createOrder(주문 생성)", orderResponse.getStoreId(), orderResponse.getId());
    }

    // 주문 상태 변경 메서드
    @Around("updateOrderStateMethod()")
    public Object logUpdateOrderStateAction(ProceedingJoinPoint joinPoint) throws Throwable {
        Long orderId = null;

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                if (orderId == null) {
                    orderId = (Long) arg;
                }
            }
        }
        Long storeId = orderRepository.findStoreIdById(orderId);

        logOrderAction("updateOrderState(주문 상태 변경)", storeId, orderId);

        return joinPoint.proceed();
    }

    // 공통적인 로그 기록 로직
    private void logOrderAction(String action, Long storeId, Long orderId) {
        LocalDateTime requestTime = LocalDateTime.now();

        // 로그 출력
        System.out.println("요청 시각: " + requestTime);
        if (storeId != null) {
            System.out.println("가게 id: " + storeId);
        }
        if (orderId != null) {
            System.out.println("주문 id: " + orderId);
        }
        System.out.println(action + " 메서드가 실행되었습니다.");
    }
}
