package com.example.deliveryapp.domain.order.entity;

import com.example.deliveryapp.domain.common.entity.Timestamped;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState orderState = OrderState.CART;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderMenu> orderMenus = new ArrayList<>();

    @Builder
    public Order(User user, Store store, OrderState orderState) {
        this.user = user;
        this.store = store;
        this.orderState = orderState;
    }

    public void addOrderMenu(OrderMenu orderMenu) {
        orderMenus.add(orderMenu);
        orderMenu.setOrder(this);
    }

    public long calculateTotalPrice() {
        return orderMenus.stream()
                .mapToLong(OrderMenu::getPrice)
                .sum();
    }
}
