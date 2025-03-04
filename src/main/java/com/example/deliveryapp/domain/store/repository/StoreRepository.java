package com.example.deliveryapp.domain.store.repository;

import com.example.deliveryapp.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {

}
