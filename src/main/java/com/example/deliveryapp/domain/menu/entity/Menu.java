package com.example.deliveryapp.domain.menu.entity;

import com.example.deliveryapp.domain.common.entity.Timestamped;
import com.example.deliveryapp.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    private String description;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Builder
    public Menu(String name, Long price, String description, Store store) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.store = store;
    }

    public void update(String menuName, Long price, String description) {
        this.name = menuName;
        this.price = price;
        this.description = description;
    }
}
