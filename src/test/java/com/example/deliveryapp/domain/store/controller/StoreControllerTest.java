package com.example.deliveryapp.domain.store.controller;

import com.example.deliveryapp.domain.store.dto.request.StoreSaveRequest;
import com.example.deliveryapp.domain.store.dto.request.StoreUpdateRequest;
import com.example.deliveryapp.domain.store.dto.response.StorePageResponse;
import com.example.deliveryapp.domain.store.dto.response.StoreResponse;
import com.example.deliveryapp.domain.store.dto.response.StoreSaveResponse;
import com.example.deliveryapp.domain.store.dto.response.StoreUpdateResponse;
import com.example.deliveryapp.domain.store.service.StoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoreService storeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long userId;
    private Long storeId;
    private StoreSaveRequest storeSaveRequest;
    private StoreUpdateRequest storeUpdateRequest;

    @BeforeEach
    void setUp() {
        userId = 1L;
        storeId = 1L;
        storeSaveRequest = StoreSaveRequest.builder()
                .name("Test Store")
                .openTime(LocalTime.of(10, 0).format(DateTimeFormatter.ofPattern("HH:mm")))
                .closeTime(LocalTime.of(20, 0).format(DateTimeFormatter.ofPattern("HH:mm")))
                .minimumOrderPrice(10000L)
                .build();

        storeUpdateRequest = StoreUpdateRequest.builder()
                .name("Updated Test Store")
                .openTime(LocalTime.of(9, 0).format(DateTimeFormatter.ofPattern("HH:mm")))
                .closeTime(LocalTime.of(19, 0).format(DateTimeFormatter.ofPattern("HH:mm")))
                .minimumOrderPrice(15000L)
                .build();
    }

    @Test
    void saveStore() throws Exception {
        StoreSaveResponse response = StoreSaveResponse.builder().id(storeId).build();
        when(storeService.save(eq(userId), any(StoreSaveRequest.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/stores")
                .requestAttr("LOGIN_USER", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(storeSaveRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(storeId));
    }

    @Test
    void findOneStore() throws Exception {
        StoreResponse response = StoreResponse.builder().id(storeId).name("Test Store").build();
        when(storeService.findOne(storeId)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/stores/{storeId}", storeId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(storeId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Store"));
    }

    @Test
    void updateStore() throws Exception {
        StoreUpdateResponse response = StoreUpdateResponse.builder().id(storeId).name("Test Store").build();
        when(storeService.update(eq(storeId), eq(userId), any(StoreUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/stores/{storeId}", storeId)
                        .requestAttr("LOGIN_USER", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(storeUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(storeId));
    }

    @Test
    void deleteStore() throws Exception {
        doNothing().when(storeService).delete(storeId, userId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/stores/{storeId}", storeId)
                        .requestAttr("LOGIN_USER", userId))
                .andExpect(status().isOk());
    }

    @Test
    void findAllPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        StorePageResponse storePageResponse = StorePageResponse.builder().id(storeId).name("Test Store").build();
        Page<StorePageResponse> page = new PageImpl<>(Collections.singletonList(storePageResponse), pageable, 1);

        when(storeService.findAllPage(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/page"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(storeId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("Test Store"));
    }

}