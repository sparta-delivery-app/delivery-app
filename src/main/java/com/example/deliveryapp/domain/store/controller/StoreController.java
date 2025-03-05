package com.example.deliveryapp.domain.store.controller;

import com.example.deliveryapp.domain.store.dto.request.StoreSaveRequest;
import com.example.deliveryapp.domain.store.dto.request.StoreUpdateRequest;
import com.example.deliveryapp.domain.store.dto.response.StorePageResponse;
import com.example.deliveryapp.domain.store.dto.response.StoreResponse;
import com.example.deliveryapp.domain.store.dto.response.StoreSaveResponse;
import com.example.deliveryapp.domain.store.dto.response.StoreUpdateResponse;
import com.example.deliveryapp.domain.store.service.StoreService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 가게 생성
    @PostMapping("/stores")
    public ResponseEntity<StoreSaveResponse> save(HttpServletRequest request, @RequestBody StoreSaveRequest dto
    ) {
        Long userId = (Long) request.getAttribute("LOGIN_USER");
        return ResponseEntity.ok(storeService.save(userId, dto));
    }

//    // 가게 페이지 API
//    @GetMapping("/page")
//    public ResponseEntity<Page<StorePageResponse>> findAllPage(@PageableDefault(size =10) Pageable pageable) {
//        Page<StorePageResponse> result = storeService.findAllPage(pageable);
//        return ResponseEntity.ok(result);
//    }

    // 가게 단건 조회
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<StoreResponse> findOne(@PathVariable Long storeId) {
        return ResponseEntity.ok(storeService.findOne(storeId));
    }

    // 가게 수정
    @PutMapping("/stores/{storeId}")
    public ResponseEntity<StoreUpdateResponse> update(
            HttpServletRequest request,
            @PathVariable Long storeId,
            @RequestBody StoreUpdateRequest dto
    ) {
        Long userId = (Long) request.getAttribute("LOGIN_USER");
        return ResponseEntity.ok(storeService.update(storeId, userId, dto));
    }

    // 가게 삭제
    @DeleteMapping("/stores/{storeId}")
    public ResponseEntity<Void> delete(
            HttpServletRequest request,
            @PathVariable Long storeId
    ) {
        Long userId = (Long) request.getAttribute("LOGIN_USER");
        storeService.delete(storeId, userId);
        return ResponseEntity.ok().build();
    }
}
