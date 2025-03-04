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
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USER006", "비밀번호가 올바르지 않습니다."),
    OWNER_ONLY_ACCESS(HttpStatus.FORBIDDEN, "USER007", "OWNER 권한이 필요합니다"),

    // STORE
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE001", "가게를 찾을 수 없습니다"),
    NOT_STORE_OWNER(HttpStatus.FORBIDDEN, "STORE002", "해당 가게에 대한 권한이 없습니다"),

    // MENU
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU001", "메뉴를 찾을 수 없습니다"),
    NOT_STORE_MENU(HttpStatus.BAD_REQUEST, "MENU002", "해당 가게에 속한 메뉴가 아닙니다"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
