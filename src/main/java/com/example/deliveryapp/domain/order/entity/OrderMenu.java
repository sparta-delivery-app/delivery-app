package com.example.deliveryapp.domain.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "order_menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderMenu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long menuId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Builder
    public OrderMenu(Order order, String name, Long price) {
        this.order = order;
        this.name = name;
        this.price = price;
    }
}
