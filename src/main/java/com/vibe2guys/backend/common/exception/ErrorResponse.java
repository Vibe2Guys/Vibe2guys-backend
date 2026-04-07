package com.vibe2guys.backend.common.exception;

public record ErrorResponse(
        boolean success,
        String message,
        String errorCode
) {

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(false, message, errorCode.getCode());
    }
}
