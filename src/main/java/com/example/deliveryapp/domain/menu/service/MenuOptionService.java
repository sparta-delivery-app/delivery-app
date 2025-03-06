package com.example.deliveryapp.domain.menu.service;

import com.example.deliveryapp.domain.menu.converter.OptionCategoryConverter;
import com.example.deliveryapp.domain.menu.dto.response.OptionCategoryPageResponse;
import com.example.deliveryapp.domain.menu.entity.OptionCategory;
import com.example.deliveryapp.domain.menu.repository.OptionCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuOptionService {

    private final OptionCategoryRepository optionCategoryRepository;

    @Transactional(readOnly = true)
    public OptionCategoryPageResponse getMenuOptions(Long menuId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<OptionCategory> optionCategoryPage = optionCategoryRepository.findAllByMenuId(menuId, pageable);
        return OptionCategoryConverter.toResponse(optionCategoryPage);
    }
}
