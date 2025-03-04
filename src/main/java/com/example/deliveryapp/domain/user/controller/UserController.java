package com.example.deliveryapp.domain.user.controller;

import com.example.deliveryapp.domain.common.annotation.Auth;
import com.example.deliveryapp.domain.common.dto.AuthUser;
import com.example.deliveryapp.domain.user.dto.request.SignInRequest;
import com.example.deliveryapp.domain.user.dto.request.SignUpRequest;
import com.example.deliveryapp.domain.user.dto.request.UserDeleteRequest;
import com.example.deliveryapp.domain.user.dto.response.SignInResponse;
import com.example.deliveryapp.domain.user.dto.response.SignUpResponse;
import com.example.deliveryapp.domain.user.dto.response.UserResponse;
import com.example.deliveryapp.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @DeleteMapping("/users")
    public ResponseEntity<Void> deleteUser(@Auth AuthUser authUser, @Valid @RequestBody UserDeleteRequest userDeleteRequest) {
        userService.deleteUser(authUser.getId(), userDeleteRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
