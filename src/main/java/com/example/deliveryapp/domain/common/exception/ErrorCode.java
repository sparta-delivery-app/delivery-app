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

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER001", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "USER002", "중복된 이메일입니다"),
    EMAIL_ALREADY_DELETED(HttpStatus.CONFLICT, "USER003", "이미 탈퇴한 이메일입니다"),
    INVALID_USER_ROLE(HttpStatus.BAD_REQUEST,"USER004","유효하지 않은 UserRole"),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED,"USER005","유효하지 않은 토큰 값입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER006", "비밀번호가 올바르지 않습니다."),


    // STORE
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE001", "가게를 찾을 수 없습니다"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
