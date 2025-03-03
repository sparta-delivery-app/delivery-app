package com.example.deliveryapp.domain.user.controller;

import com.example.deliveryapp.domain.user.dto.request.SignInRequest;
import com.example.deliveryapp.domain.user.dto.request.SignUpRequest;
import com.example.deliveryapp.domain.user.dto.request.UserDeleteRequest;
import com.example.deliveryapp.domain.user.dto.response.SignInResponse;
import com.example.deliveryapp.domain.user.dto.response.SignUpResponse;
import com.example.deliveryapp.domain.user.dto.response.UserResponse;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.example.deliveryapp.domain.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void 회원가입_성공() throws Exception {

        SignUpRequest signUpRequest = new SignUpRequest("em@em.com", "pw", "name", "OWNER");
        SignUpResponse signUpResponse = new SignUpResponse("bearerToken");

        given(userService.signUp(any(SignUpRequest.class))).willReturn(signUpResponse);

        mockMvc.perform(post("/users/signup")
                        .content(objectMapper.writeValueAsString(signUpRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("bearerToken"));
    }

    @Test
    void 로그인_성공() throws Exception {
        // given
        SignInRequest signInRequest = new SignInRequest("em@em.com", "password");
        SignInResponse signInResponse = new SignInResponse("bearerToken");
        given(userService.signIn(any(SignInRequest.class))).willReturn(signInResponse);

        // when&then
        mockMvc.perform(post("/users/signin")
                        .content(objectMapper.writeValueAsString(signInRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("bearerToken"));
    }

    @Test
    void 사용자_조회() throws Exception {
        //given
        long userId = 1L;
        String email = "email@email.com";
        User user = new User(email, "password", "name", UserRole.OWNER);
        ReflectionTestUtils.setField(user, "id", userId);
        given(userService.getUser(userId)).willReturn(new UserResponse(user));

        //when&then
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void 회원_탈퇴() throws Exception {
        Long userId = 1L;
        String bearerToken = "bearerToken";

        UserDeleteRequest userDeleteRequest = new UserDeleteRequest("password");

        doNothing().when(userService).deleteUser(userId, userDeleteRequest);

        mockMvc.perform(delete("/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDeleteRequest)))
                .andExpect(status().isOk());
    }
}
