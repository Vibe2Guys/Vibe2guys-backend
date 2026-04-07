package com.vibe2guys.backend.common.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError == null ? "입력값이 올바르지 않습니다." : fieldError.getDefaultMessage();
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, ex.getMessage()));
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(Exception ex) {
        return ResponseEntity
                .status(ErrorCode.INVALID_CREDENTIALS.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex) {
        return ResponseEntity
                .status(ErrorCode.USER_INACTIVE.getStatus())
                .body(ErrorResponse.of(ErrorCode.USER_INACTIVE, "비활성 사용자입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
    }
}
