package com.example.deliveryapp.domain.user.service;


import com.example.deliveryapp.config.JwtUtil;
import com.example.deliveryapp.config.PasswordEncoder;
import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.user.dto.request.SignUpRequest;
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
    public String signUp(SignUpRequest signUpRequest) {
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

        return jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getName(), userRole);
    }
}
