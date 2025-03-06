package com.example.deliveryapp.domain.order.entity;

import com.example.deliveryapp.domain.menu.entity.OptionItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "order_menu_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderMenuOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_menu_id", nullable = false)
    private OrderMenu orderMenu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_item_id")
    private OptionItem optionItem;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long additionalPrice;

    public OrderMenuOption(OptionItem optionItem) {
        this.optionItem = optionItem;
        this.name = optionItem.getName();
        this.additionalPrice = optionItem.getAdditionalPrice();
    }
}
