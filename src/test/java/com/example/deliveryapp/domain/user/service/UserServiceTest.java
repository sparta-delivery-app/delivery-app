package com.example.deliveryapp.domain.user.service;

import com.example.deliveryapp.config.JwtUtil;
import com.example.deliveryapp.config.PasswordEncoder;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.user.dto.request.SignInRequest;
import com.example.deliveryapp.domain.user.dto.request.SignUpRequest;
import com.example.deliveryapp.domain.user.dto.request.UserDeleteRequest;
import com.example.deliveryapp.domain.user.dto.response.SignInResponse;
import com.example.deliveryapp.domain.user.dto.response.SignUpResponse;
import com.example.deliveryapp.domain.user.dto.response.UserResponse;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    class signUp {
        @Test
        void 이미_존재하는_이메일로_회원가입시_예외_발생() {
            SignUpRequest signUpRequest = new SignUpRequest("email@email.com","password","name","OWNER");

            given(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).willReturn(true);

            assertThrows(CustomException.class,()->userService.signUp(signUpRequest),"유효하지 않은 요청 값입니다");
        }

        @Test
        void 탈퇴한_이메일로_회원가입시_예외_발생() {
            SignUpRequest signUpRequest = new SignUpRequest("email@email.com","password","name","OWNER");

            given(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).willReturn(false);
            given(userRepository.existsByEmailAndDeletedAtIsNotNull(anyString())).willReturn(true);

            assertThrows(CustomException.class,()->userService.signUp(signUpRequest),"이미 탈퇴한 이메일입니다");
        }

        @Test
        void 회원가입_성공() {
            long userId = 1L;

            SignUpRequest signUpRequest = new SignUpRequest("email@email.com","password","name","OWNER");

            given(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).willReturn(false);
            given(userRepository.existsByEmailAndDeletedAtIsNotNull(anyString())).willReturn(false);
            given(passwordEncoder.encode(signUpRequest.getPassword())).willReturn("encodedPassword");

            User user = new User(
                    signUpRequest.getEmail(),
                    passwordEncoder.encode(signUpRequest.getPassword()),
                    signUpRequest.getName(),
                    UserRole.of(signUpRequest.getUserRole())
            );
            ReflectionTestUtils.setField(user,"id",userId);

            given(userRepository.save(any(User.class))).willReturn(user);
            given(jwtUtil.createToken(anyLong(), anyString(), anyString(), any(UserRole.class))).willReturn("BearerToken");

            SignUpResponse signUpResponse = userService.signUp(signUpRequest);

            assertNotNull(signUpResponse);
            assertEquals("BearerToken", signUpResponse.getBearerToken());
            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    @Nested
    class signIn {
        @Test
        void 존재하지_않는_이메일로_로그인시_예외_발생() {
            SignInRequest signInRequest = new SignInRequest("email@email.com","password");

            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

            assertThrows(CustomException.class,()->userService.signIn(signInRequest),"사용자를 찾을 수 없습니다");
        }

        @Test
        void 비밀번호_불일치시_예외_발생() {
            long userId = 1L;
            User user = new User("email@email.com","password","name",UserRole.OWNER);
            SignInRequest signInRequest = new SignInRequest("email@email.com","password");
            ReflectionTestUtils.setField(user,"id",userId);

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(),anyString())).willReturn(false);

            assertThrows(CustomException.class,()->userService.signIn(signInRequest),"비밀번호가 올바르지 않습니다.");
        }

        @Test
        void 로그인_성공() {
            long userId = 1L;
            User user = new User("email@email.com","password","name",UserRole.OWNER);
            SignInRequest signInRequest = new SignInRequest("email@email.com","password");
            ReflectionTestUtils.setField(user,"id",userId);

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(),anyString())).willReturn(true);
            given(jwtUtil.createToken(anyLong(), anyString(), anyString(), any(UserRole.class))).willReturn("BearerToken");

            SignInResponse signInResponse = userService.signIn(signInRequest);

            assertNotNull(signInResponse);
            assertEquals("BearerToken", signInResponse.getBearerToken());
        }
    }

    @Nested
    class getUser {
        @Test
        void 사용자_id가_존재하지_않을시_예외_발생() {
            long userId = 1L;

            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThrows(CustomException.class,()->userService.getUser(userId),"사용자를 찾을 수 없습니다");
        }

        @Test
        void 사용자_id로_프로필_조회_성공() {
            long userId = 1L;
            String email = "email@email.com";
            User user = new User(email,"password","name",UserRole.OWNER);
            ReflectionTestUtils.setField(user,"id",userId);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            UserResponse userResponse = userService.getUser(userId);

            assertNotNull(userResponse);
            assertEquals(userResponse.getId(),userId);
            assertEquals(userResponse.getEmail(),email);
        }
    }

    @Nested
    class deleteUser {
        @Test
        void 탈퇴_비밀번호_불일치시_예외_발생() {
            long userId = 1L;
            String email = "email@email.com";
            User user = new User(email, "password", "name", UserRole.OWNER);
            ReflectionTestUtils.setField(user, "id", userId);

            UserDeleteRequest userDeleteRequest = new UserDeleteRequest("password");

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            assertThrows(CustomException.class,()->userService.deleteUser(userId,userDeleteRequest),"비밀번호가 올바르지 않습니다.");
        }

        @Test
        void 사용자_탈퇴_성공() {
            long userId = 1L;
            String email = "email@email.com";
            User user = new User(email, "password", "name", UserRole.OWNER);
            ReflectionTestUtils.setField(user, "id", userId);

            UserDeleteRequest userDeleteRequest = new UserDeleteRequest("password");

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);

            userService.deleteUser(userId, userDeleteRequest);

            assertNotNull(user.getDeletedAt());
            verify(userRepository, times(1)).save(any(User.class));
        }
    }
}
