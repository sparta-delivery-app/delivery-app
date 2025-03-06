package com.example.deliveryapp.domain.menu.repository;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.entity.OptionCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OptionCategoryRepository extends JpaRepository<OptionCategory, Long> {

    @EntityGraph(attributePaths = "optionItems")
    Optional<OptionCategory> findByIdAndMenuId(Long id, Long menuId);

    default OptionCategory findByIdAndMenuIdOrThrow(Long id, Long menuId) {
        return findByIdAndMenuId(id, menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPTION_NOT_FOUND));
    }

    @EntityGraph(attributePaths = "optionItems")
    Page<OptionCategory> findAllByMenuId(Long menuId, Pageable pageable);
}
