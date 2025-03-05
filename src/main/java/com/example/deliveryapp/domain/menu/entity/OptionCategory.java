package com.example.deliveryapp.domain.menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "option_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OptionCategory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean isRequired;

    @Column(nullable = false)
    private Boolean isMultiple;

    private Integer maxOptions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @OneToMany(mappedBy = "optionCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OptionItem> optionItems = new ArrayList<>();

    @Builder
    public OptionCategory(String name, Boolean isRequired, Boolean isMultiple, Integer maxOptions, Menu menu) {
        this.name = name;
        this.isRequired = isRequired;
        this.isMultiple = isMultiple;
        this.maxOptions = maxOptions;
        this.menu = menu;
    }

    public void addOptionItem(OptionItem optionItem) {
        this.optionItems.add(optionItem);
        optionItem.setOptionCategory(this);
    }
}
