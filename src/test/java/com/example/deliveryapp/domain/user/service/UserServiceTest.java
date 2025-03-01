package com.example.deliveryapp.domain.user.service;

import com.example.deliveryapp.config.JwtUtil;
import com.example.deliveryapp.config.PasswordEncoder;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.user.dto.request.SignUpRequest;
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
    class signup {
        @Test
        void 이미_존재하는_이메일로_회원가입시_예외_발생() {

            SignUpRequest signUpRequest = new SignUpRequest("email@email.com","password","name","OWNER");

            given(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).willReturn(true);

            assertThrows(CustomException.class,()->userService.signUp(signUpRequest),"유효하지 않은 요청 값입니다");
        }

        @Test
        void 탈퇴한_이메일로_회원가입시_예외_발생() {

            SignUpRequest signUpRequest = new SignUpRequest("email@email.com","password","name","OWNER");

            given(userRepository.existsByEmailAndDeletedAtIsNotNull(anyString())).willReturn(false);
            given(userRepository.existsByEmailAndDeletedAtIsNotNull(anyString())).willReturn(true);

            assertThrows(CustomException.class,()->userService.signUp(signUpRequest),"이미 탈퇴한 이메일입니다");
        }

        @Test
        void 회원가입_성공() {

            long userId = 1L;

            SignUpRequest signUpRequest = new SignUpRequest("email@email.com","password","name","OWNER");

            given(userRepository.existsByEmailAndDeletedAtIsNotNull(anyString())).willReturn(false);
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

            String token = userService.signUp(signUpRequest);

            assertNotNull(token);
            assertEquals("BearerToken",token);
            verify(userRepository,times(1)).save(any(User.class));
        }
    }
}
