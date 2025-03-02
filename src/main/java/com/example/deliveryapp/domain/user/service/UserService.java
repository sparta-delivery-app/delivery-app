package com.example.deliveryapp.domain.user.service;


import com.example.deliveryapp.config.JwtUtil;
import com.example.deliveryapp.config.PasswordEncoder;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.user.dto.request.SignInRequest;
import com.example.deliveryapp.domain.user.dto.request.SignUpRequest;
import com.example.deliveryapp.domain.user.dto.response.SignInResponse;
import com.example.deliveryapp.domain.user.dto.response.SignUpResponse;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignUpResponse signUp(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(signUpRequest.getEmail())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (userRepository.existsByEmailAndDeletedAtIsNotNull(signUpRequest.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_DELETED);
        }

        UserRole userRole = UserRole.of(signUpRequest.getUserRole());

        User newUser = new User(
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.getName(),
                userRole
        );

        User savedUser = userRepository.save(newUser);

        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getName(), userRole);

        return new SignUpResponse(bearerToken);
    }

    @Transactional
    public SignInResponse signIn(SignInRequest signInRequest) {
        User user = userRepository.findByEmail(signInRequest.getEmail()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(signInRequest.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getName(), user.getRole());

        return new SignInResponse(bearerToken);
    }
}
