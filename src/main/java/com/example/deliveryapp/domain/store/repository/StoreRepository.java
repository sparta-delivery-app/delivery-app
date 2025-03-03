package com.example.deliveryapp.domain.store.repository;

import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findAllByUser(User user);

    @Modifying
    @Transactional
    @Query("UPDATE Store s SET s.isDeleted = true WHERE s.user.id = :id")
    int softdeleteByUserId(Long id);
}
