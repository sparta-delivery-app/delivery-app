package com.example.deliveryapp.domain.menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "option_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OptionItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long additionalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_category_id", nullable = false)
    private OptionCategory optionCategory;

    @Builder
    public OptionItem(String name, Long additionalPrice) {
        this.name = name;
        this.additionalPrice = additionalPrice;
    }

    public void setOptionCategory(OptionCategory optionCategory) {
        this.optionCategory = optionCategory;
    }
}
