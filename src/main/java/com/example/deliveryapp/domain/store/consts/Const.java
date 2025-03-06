package com.example.deliveryapp.domain.store.consts;

public class Const {
    public static final String STORE_NAME_NOT_NULL = "가게 이름은 필수 값입니다.";
    public static final String STORE_NAME_SIZE = "가게 이름은 최대 20자 입니다.";

    public static final String OPEN_TIME_NOT_NULL = "오픈 시간은 필수 값입니다.";
    public static final String OPEN_TIME_PATTERN = "^([01][0-9]|2[0-3]):([0-5][0-9])$";
    public static final String OPEN_TIME_REQUIREMENT = "오픈 시간 형식이 올바르지 않습니다. (HH:mm)";

    public static final String CLOSE_TIME_NOT_NULL = "마감 시간은 필수 값입니다.";
    public static final String CLOSE_TIME_PATTERN = "^([01][0-9]|2[0-3]):([0-5][0-9])$";
    public static final String CLOSE_TIME_REQUIREMENT = "마감 시간 형식이 올바르지 않습니다. (HH:mm)";

    public static final String MINIMUM_ORDER_PRICE_NOT_NULL = "최소 주문 금액은 필수 값입니다.";
    public static final String MINIMUM_ORDER_PRICE_REQUIREMENT = "최소 주문 가격은 0 이상이어야 합니다.";

    public static final String STORE_STATUS_NOT_NULL = "가게 상태는 필수 값입니다.";
}
