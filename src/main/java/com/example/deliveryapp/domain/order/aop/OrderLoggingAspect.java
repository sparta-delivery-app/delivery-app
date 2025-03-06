package com.example.deliveryapp.domain.order.aop;

import com.example.deliveryapp.domain.order.dto.response.OrderResponse;
import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class OrderLoggingAspect {

    private final OrderRepository orderRepository;

    @Pointcut("execution(* com.example.deliveryapp.domain.order.controller.OrderController.createOrder(..))")
    public void createOrderMethod() {
    }

    @Pointcut("execution(* com.example.deliveryapp.domain.order.controller.OrderController.updateOrderState(..))")
    public void updateOrderStateMethod() {
    }

    @Around("createOrderMethod()")
    public Object logCreateOrderAction(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof ResponseEntity<?> responseEntity) {
            if (responseEntity.getBody() instanceof OrderResponse orderResponse) {
                logOrderAction("createOrder(주문 생성)", orderResponse.getStoreId(), orderResponse.getOrderId());
            } else {
                System.out.println("주문 생성 로그 기록 실패: ResponseEntity의 body가 OrderResponse가 아님");
            }
        } else {
            System.out.println("주문 생성 로그 기록 실패: 반환값이 ResponseEntity가 아님");
        }

        return result;
    }

    @Around("updateOrderStateMethod()")
    public Object logUpdateOrderStateAction(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long orderId = null;
        OrderState prevState = null;

        for (Object arg : args) {
            if (arg instanceof Long) {
                orderId = (Long) arg;
                break;
            }
        }

        if (orderId != null) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                prevState = order.getOrderState(); // 변경 전 상태 저장
            }
        }

        Object result = joinPoint.proceed();

        // 변경 후 상태
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            logOrderStateChange(orderId, order.getStore().getId(), prevState, order.getOrderState());
        }

        return result;
    }

    private void logOrderAction(String action, Long storeId, Long orderId) {
        LocalDateTime requestTime = LocalDateTime.now();

        System.out.println("요청 시각: " + requestTime);
        if (storeId != null) {
            System.out.println("가게 ID: " + storeId);
        }
        if (orderId != null) {
            System.out.println("주문 ID: " + orderId);
        }
        System.out.println(action + " 메서드가 실행되었습니다.");
    }

    private void logOrderStateChange(Long orderId, Long storeId, OrderState prevState, OrderState newState) {
        LocalDateTime requestTime = LocalDateTime.now();

        System.out.println("요청 시각: " + requestTime);
        System.out.println("주문 ID: " + orderId);
        System.out.println("가게 ID: " + storeId);
        System.out.println("변경 전 상태: " + prevState);
        System.out.println("변경 후 상태: " + newState);
        System.out.println("updateOrderState(주문 상태 변경) 메서드가 실행되었습니다.");
    }
}
