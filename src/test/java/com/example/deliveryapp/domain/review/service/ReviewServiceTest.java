package com.example.deliveryapp.domain.review.service;

import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.review.dto.ReviewRequestDto;
import com.example.deliveryapp.domain.review.dto.ReviewResponseDto;
import com.example.deliveryapp.domain.review.entity.Review;
import com.example.deliveryapp.domain.review.repository.ReviewRepository;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import com.example.deliveryapp.domain.order.repository.OrderMenuRepository;
import com.example.deliveryapp.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMenuRepository orderMenuRepository;

    @InjectMocks
    private ReviewService reviewService;

    // 1. 리뷰 생성 성공
    @Test
    @Transactional
    public void 리뷰_생성_성공() {
        // given
        Long orderId = 1L;
        String content = "Great food!";
        Integer rating = 5;

        // ReviewRequestDto 객체 생성
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(orderId, content, rating);

        // User 객체를 빌더 패턴을 사용하여 생성
        User user = new User("user@example.com", "password", "userName", UserRole.USER);

        // ReflectionTestUtils을 사용하여 User 객체에 ID 설정
        Long userId = 1L;
        ReflectionTestUtils.setField(user, "id", userId);

        // Order 객체 생성 (OrderState: COMPLETED 상태)
        Order order = new Order(user, null, OrderState.COMPLETED);

        // ReflectionTestUtils을 사용하여 Order 객체에 ID 설정
        ReflectionTestUtils.setField(order, "id", orderId);

        // Review 객체 생성
        Review review = new Review(user, order, content, rating);

        // ReviewRepository와 OrderRepository의 동작 정의
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(userRepository.findById(order.getUser().getId())).willReturn(Optional.of(user));
        given(reviewRepository.save(any(Review.class))).willReturn(review);

        // when
        ReviewResponseDto result = reviewService.createReview(reviewRequestDto);

        // then
        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(rating, result.getRating());
    }

    // 2. 배달 완료되지 않은 주문에 대한 리뷰 작성 시 오류 발생
    @Test
    public void 리뷰_생성_배달완료되지않은_주문() {
        // given
        Long orderId = 1L;
        String content = "Great food!";
        Integer rating = 5;

        // ReviewRequestDto 객체 생성
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(orderId, content, rating);

        // User 객체 생성
        User user = new User("user@example.com", "password", "userName", UserRole.USER);
        Long userId = 1L;
        ReflectionTestUtils.setField(user, "id", userId);

        // Order 객체 생성 (배달 완료되지 않음)
        Order order = new Order(user, null, OrderState.PENDING);
        ReflectionTestUtils.setField(order, "id", orderId);

        // OrderRepository의 동작 정의
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(reviewRequestDto);
        });

        // then
        assertEquals("배달 완료되지 않은 주문에 대해서는 리뷰를 작성할 수 없습니다.", exception.getMessage());
    }

    // 3. 주문을 찾을 수 없는 경우
    @Test
    public void 리뷰_생성_주문을_찾을_수_없는_경우() {
        // given
        Long orderId = 1L;
        String content = "Great food!";
        Integer rating = 5;

        // ReviewRequestDto 객체 생성
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(orderId, content, rating);

        // 주문이 존재하지 않는 경우
        given(orderRepository.findById(orderId)).willReturn(Optional.empty());

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(reviewRequestDto);
        });

        // then
        assertEquals("주문을 찾을 수 없습니다.", exception.getMessage());
    }

    // 4. 사용자를 찾을 수 없는 경우
    @Test
    public void 리뷰_생성_사용자를_찾을_수_없는_경우() {
        // given
        Long orderId = 1L;
        String content = "Great food!";
        Integer rating = 5;

        // ReviewRequestDto 객체 생성
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(orderId, content, rating);

        // User 객체 생성
        User user = new User("user@example.com", "password", "userName", UserRole.USER);
        Long userId = 1L;
        ReflectionTestUtils.setField(user, "id", userId);

        // Order 객체 생성
        Order order = new Order(user, null, OrderState.COMPLETED);
        ReflectionTestUtils.setField(order, "id", orderId);

        // 주문은 있지만 사용자를 찾을 수 없는 경우
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(userRepository.findById(order.getUser().getId())).willReturn(Optional.empty());

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(reviewRequestDto);
        });

        // then
        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
    }

    // 5. 리뷰 저장 실패 시 처리
    @Test
    public void 리뷰_생성_저장_실패() {
        // given
        Long orderId = 1L;
        String content = "Great food!";
        Integer rating = 5;

        // ReviewRequestDto 객체 생성
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(orderId, content, rating);

        // User 객체 생성
        User user = new User("user@example.com", "password", "userName", UserRole.USER);
        Long userId = 1L;
        ReflectionTestUtils.setField(user, "id", userId);

        // Order 객체 생성 (배달 완료 상태)
        Order order = new Order(user, null, OrderState.COMPLETED);
        ReflectionTestUtils.setField(order, "id", orderId);

        // ReviewRepository의 동작 정의 (리뷰 저장 실패 시뮬레이션)
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(userRepository.findById(order.getUser().getId())).willReturn(Optional.of(user));
        given(reviewRepository.save(any(Review.class))).willThrow(new RuntimeException("리뷰 저장 실패"));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(reviewRequestDto);
        });

        // then
        assertEquals("리뷰 저장 실패", exception.getMessage());
    }
}
