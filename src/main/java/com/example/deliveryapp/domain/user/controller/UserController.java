package com.example.deliveryapp.domain.user.controller;

import com.example.deliveryapp.domain.user.dto.request.SignInRequest;
import com.example.deliveryapp.domain.user.dto.request.SignUpRequest;
import com.example.deliveryapp.domain.user.dto.response.SignInResponse;
import com.example.deliveryapp.domain.user.dto.response.SignUpResponse;
import com.example.deliveryapp.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users/signup")
    public SignUpResponse signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        return userService.signUp(signUpRequest);
    }

    @PostMapping("/users/signin")
    public SignInResponse signIn(@Valid @RequestBody SignInRequest signInRequest) {
        return userService.signIn(signInRequest);
    }


}
