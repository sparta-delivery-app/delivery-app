package com.example.deliveryapp.domain.store.service;

import com.example.deliveryapp.domain.store.repository.StoreRepository;
import com.example.deliveryapp.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreDeleteService {

    private final StoreRepository storeRepository;

    @Transactional
    public void delete(User user) {
        storeRepository.softdeleteByUserId(user.getId());
    }
}
