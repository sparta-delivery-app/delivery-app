package com.example.deliveryapp.domain.order.dto.response;

import com.example.deliveryapp.domain.order.enums.OrderState;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private final Long orderId;
    private final Long storeId;
    private final OrderState orderState;
    private final Long totalPrice;
    private final List<OrderMenuResponse> orderMenus;

    @Getter
    @AllArgsConstructor
    public static class OrderMenuResponse {
        private final Long orderMenuId;
        private final Long menuId;
        private final String menuName;
        private final Long price;
        private final List<OrderMenuOptionResponse> orderMenuOptions;

        @Getter
        @AllArgsConstructor
        public static class OrderMenuOptionResponse {
            private final Long orderMenuOptionId;
            private final Long optionItemId;
            private final String optionItemName;
            private final Long additionalPrice;
        }
    }
}
