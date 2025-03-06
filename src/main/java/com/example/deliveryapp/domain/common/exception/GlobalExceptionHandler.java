package com.example.deliveryapp.domain.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        ErrorResponse body = ErrorResponse.of(ex.getErrorCode());
        return new ResponseEntity<>(body, ex.getHttpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("[서버 오류 발생] ", ex);
        ErrorResponse body = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (oldMsg, newMsg) -> oldMsg + ", " + newMsg // 중복 필드 오류 메시지 처리
                ));

        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_REQUEST, errors);
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = ex.getParameterValidationResults().stream()
                .collect(Collectors.toMap(
                        result -> result.getMethodParameter().getParameterName(),
                        result -> result.getResolvableErrors().stream()
                                .map(MessageSourceResolvable::getDefaultMessage)
                                .collect(Collectors.joining(", "))
                ));

        ErrorResponse body = ErrorResponse.of(ErrorCode.INVALID_REQUEST, errors);
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorResponse body = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

}
