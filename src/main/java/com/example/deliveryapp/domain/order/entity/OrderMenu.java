package com.example.deliveryapp.domain.order.entity;

import com.example.deliveryapp.domain.common.entity.Timestamped;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "order_menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long menuId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Builder
    public OrderMenu(Order order, Long menuId, String name, Long price) {
        this.order = order;
        this.menuId = menuId;
        this.name = name;
        this.price = price;
    }
}
