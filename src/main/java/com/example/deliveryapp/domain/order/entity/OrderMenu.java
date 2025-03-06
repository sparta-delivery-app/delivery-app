package com.example.deliveryapp.domain.order.entity;

import com.example.deliveryapp.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "order_menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @OneToMany(mappedBy = "orderMenu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderMenuOption> orderMenuOptions = new ArrayList<>();

    public OrderMenu(Menu menu) {
        this.menu = menu;
        this.name = menu.getName();
        this.price = menu.getPrice();
    }

    public void addOrderMenuOption(OrderMenuOption orderMenuOption) {
        this.orderMenuOptions.add(orderMenuOption);
        orderMenuOption.setOrderMenu(this);
    }

    public Long getTotalPrice() {
        long additionalPrice = this.orderMenuOptions.stream()
                .mapToLong(OrderMenuOption::getAdditionalPrice)
                .sum();
        return additionalPrice + price;
    }
}
