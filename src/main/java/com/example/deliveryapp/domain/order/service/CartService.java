package com.example.deliveryapp.domain.order.service;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.entity.OptionCategory;
import com.example.deliveryapp.domain.menu.entity.OptionItem;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.menu.repository.OptionCategoryRepository;
import com.example.deliveryapp.domain.menu.repository.OptionItemRepository;
import com.example.deliveryapp.domain.order.dto.request.CartAddRequest;
import com.example.deliveryapp.domain.order.dto.request.CartAddRequest.OptionRequest;
import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.entity.OrderMenu;
import com.example.deliveryapp.domain.order.entity.OrderMenuOption;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final OptionCategoryRepository optionCategoryRepository;
    private final OptionItemRepository optionItemRepository;

    @Transactional
    public void addCart(Long userId, CartAddRequest request) {
        User user = userRepository.findByIdAndUserRoleOrThrow(userId);
        Menu menu = menuRepository.findActiveMenuByIdOrThrow(request.getMenuId());
        List<OptionCategory> optionCategories = optionCategoryRepository.findAllByMenuId(menu.getId());

        validateOptionRequest(request, optionCategories);

        // 장바구니 찾아오고, 없으면 새로 생성
        Order cart = orderRepository.findByUserIdAndOrderState(userId, OrderState.CART)
                .orElse(new Order(user, menu.getStore(), OrderState.CART));

        // 기존 장바구니의 가게와 요청한 메뉴의 가게가 다른 경우 장바구니 초기화
        if (!cart.getStore().getId().equals(menu.getStore().getId())) {
            cart.clearOrderMenus();
            cart.setStore(menu.getStore());
        }

        // 장바구니에 주문 메뉴 추가
        OrderMenu orderMenu = new OrderMenu(menu);
        request.getOptions().stream()
                .flatMap(optionRequest -> optionRequest.getOptionItemIds().stream())
                .map(optionItemRepository::findByIdOrThrow)
                .map(OrderMenuOption::new)
                .forEach(orderMenu::addOrderMenuOption);

        cart.addOrderMenu(orderMenu);

        if (cart.getId() == null) { // 장바구니가 새로 생성된 경우
            orderRepository.save(cart);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // 자정에 장바구니 검사
    public void cleanupCarts() {
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            checkCartTimeOut(order.getId());
        }
    }

    private void checkCartTimeOut(Long orderId) {
        Order order = orderRepository.findByUserIdAndOrderState(orderId, OrderState.CART)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        Duration duration = Duration.between(order.getUpdatedAt(), LocalDateTime.now());

        if (24 <= duration.toHours()) {
            order.getOrderMenus().clear();
            orderRepository.save(order);
        }
    }

    private void validateOptionRequest(CartAddRequest request, List<OptionCategory> optionCategories) {
        Map<Long, List<Long>> selectedOptionMap = getOptionRequestMap(request.getOptions());

        if (optionCategories.size() != selectedOptionMap.size()) {
            throw new CustomException(ErrorCode.INVALID_OPTION_CATEGORY_COUNT);
        }

        for (OptionCategory optionCategory : optionCategories) {
            if (!selectedOptionMap.containsKey(optionCategory.getId())) {
                throw new CustomException(ErrorCode.MISSING_OPTION_CATEGORY);
            }

            List<Long> selectedOptionItemIds = selectedOptionMap.get(optionCategory.getId());
            if (optionCategory.getIsRequired() && selectedOptionItemIds.isEmpty()) {
                throw new CustomException(ErrorCode.REQUIRED_OPTION_NOT_SELECTED);
            }

            if (!optionCategory.getIsMultiple() && selectedOptionItemIds.size() > 1) {
                throw new CustomException(ErrorCode.INVALID_MULTIPLE_SELECTION);
            }

            if (optionCategory.getMaxOptions() != null
                    && optionCategory.getMaxOptions() < selectedOptionItemIds.size()) {
                throw new CustomException(ErrorCode.EXCEEDS_MAX_OPTION_SELECTION);
            }

            Set<Long> optionItemIds = optionCategory.getOptionItems().stream()
                    .map(OptionItem::getId)
                    .collect(Collectors.toSet());

            if (!optionItemIds.containsAll(selectedOptionItemIds)) {
                throw new CustomException(ErrorCode.INVALID_OPTION_ITEM);
            }
        }
    }

    private Map<Long, List<Long>> getOptionRequestMap(List<OptionRequest> request) {
        try {
            return request.stream()
                    .collect(Collectors.toMap(
                            OptionRequest::getOptionCategoryId,
                            OptionRequest::getOptionItemIds
                    ));
        } catch (IllegalStateException e) { // 중복 키 존재
            throw new CustomException(ErrorCode.OPTION_CATEGORY_DUPLICATE);
        }
    }
}
