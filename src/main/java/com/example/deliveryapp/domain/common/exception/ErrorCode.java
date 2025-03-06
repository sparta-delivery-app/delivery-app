package com.example.deliveryapp.domain.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // COMMON
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON001", "유효하지 않은 요청 값입니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON002", "이 엔드포인트에서는 해당 HTTP 메서드를 지원하지 않습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON003", "서버 내부 오류가 발생했습니다"),

    // TOKEN
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "TOKEN001", "유효하지 않는 JWT 서명입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN002", "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN003", "지원되지 않는 토큰입니다."),
    EMPTY_CLAIMS(HttpStatus.BAD_REQUEST, "TOKEN004", "잘못된 토큰입니다."),
    TOKEN_VERIFICATION_ERROR(HttpStatus.UNAUTHORIZED, "TOKEN005", "토큰 검증 중 오류가 발생했습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "TOKEN006", "유효하지 않은 토큰입니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER001", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "USER002", "중복된 이메일입니다"),
    EMAIL_ALREADY_DELETED(HttpStatus.CONFLICT, "USER003", "이미 탈퇴한 이메일입니다"),
    INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "USER004", "올바르지 않은 사용자 권한입니다"),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "USER005", "유효하지 않은 토큰 값입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER006", "비밀번호가 올바르지 않습니다"),
    OWNER_ONLY_ACCESS(HttpStatus.FORBIDDEN, "USER007", "OWNER 권한이 필요합니다"),

    // STORE
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE001", "가게를 찾을 수 없습니다"),
    INVALID_STORE_STATUS(HttpStatus.BAD_REQUEST, "STORE002", "현재 가게는 주문을 받을 수 없는 상태입니다"),
    INVALID_USER_UPDATE_STORE(HttpStatus.FORBIDDEN, "STORE003", "가게 수정 권한이 없습니다"),
    INVALID_USER_DELETE_STORE(HttpStatus.FORBIDDEN, "STORE004", "가게 삭제 권한이 없습니다"),
    STORE_HAS_ORDERS(HttpStatus.CONFLICT, "STORE005", "주문이 있는 가게는 삭제할 수 없습니다"),
    STORE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "STORE006", "가게는 최대 3개까지 등록할 수 있습니다"),
    STORE_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "STORE007", "이미 폐업된 가게입니다"),
    STORE_STATUS_CANNOT_BE_CHANGED_TO_CLOSED(HttpStatus.BAD_REQUEST, "STORE008", "가게 상태를 폐업으로 변경할 수 없습니다"),
    NOT_STORE_OWNER(HttpStatus.FORBIDDEN, "STORE002", "해당 가게에 대한 권한이 없습니다"),

    // MENU
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU001", "메뉴를 찾을 수 없습니다"),
    NOT_STORE_MENU(HttpStatus.BAD_REQUEST, "MENU002", "해당 가게에 속한 메뉴가 아닙니다"),

    // MENU OPTION
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "OPTION001", "옵션을 찾을 수 없습니다"),

    // FILE
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "FILE001", "빈 파일은 업로드할 수 없습니다"),
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "FILE002", "이미지 파일만 업로드할 수 있습니다"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE003", "파일 업로드에 실패하였습니다"),

    // ORDER
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER001", "주문 정보를 찾을 수 없습니다"),
    ORDER_CANNOT_BE_ACCEPTED(HttpStatus.BAD_REQUEST, "ORDER002", "대기 중인 주문만 수락할 수 있습니다"),
    ORDER_CANNOT_BE_REJECTED(HttpStatus.BAD_REQUEST, "ORDER003", "대기 중인 주문만 거절할 수 있습니다"),
    ORDER_CANNOT_BE_DELIVERY(HttpStatus.BAD_REQUEST, "ORDER004", "수락된 주문만 배달을 시작할 수 있습니다"),
    ORDER_CANNOT_BE_COMPLETED(HttpStatus.BAD_REQUEST, "ORDER005", "배달 시작된 주문만 배달 완료 처리할 수 있습니다"),
    ORDER_CANNOT_BE_CANCELED(HttpStatus.BAD_REQUEST, "ORDER006", "배달이 완료되었거나 진행 중인 주문은 취소할 수 없습니다"),
    ORDER_CANNOT_BE_CART(HttpStatus.BAD_REQUEST,"ORDER007","이미 주문된 상태입니다"),
    ORDER_CLOSED(HttpStatus.BAD_REQUEST, "ORDER008", "가게 운영 시간이 아닙니다"),
    ORDER_TOO_CHEAP(HttpStatus.BAD_REQUEST, "ORDER009", "최소 주문 금액을 만족해야 주문이 가능합니다");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
