package com.vibe2guys.backend.common.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final Map<String, String> FIELD_LABELS = Map.ofEntries(
            Map.entry("title", "제목"),
            Map.entry("description", "설명"),
            Map.entry("thumbnailUrl", "썸네일"),
            Map.entry("fileName", "파일 이름"),
            Map.entry("contentType", "파일 형식"),
            Map.entry("category", "업로드 구분"),
            Map.entry("startDate", "시작일"),
            Map.entry("endDate", "종료일"),
            Map.entry("openAt", "공개 시각"),
            Map.entry("scheduledAt", "수업 시각"),
            Map.entry("weekNumber", "주차 번호"),
            Map.entry("videoUrl", "영상 파일"),
            Map.entry("documentUrl", "문서 파일"),
            Map.entry("durationSeconds", "영상 길이"),
            Map.entry("name", "이름"),
            Map.entry("profileImageUrl", "프로필 이미지"),
            Map.entry("email", "이메일"),
            Map.entry("password", "비밀번호"),
            Map.entry("answerText", "답변"),
            Map.entry("messageBody", "메시지"),
            Map.entry("teamSize", "팀 인원")
    );

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError == null
                ? "입력값이 올바르지 않습니다."
                : humanizeValidationMessage(fieldError.getField(), fieldError.getDefaultMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        ConstraintViolation<?> violation = ex.getConstraintViolations().stream().findFirst().orElse(null);
        String message = violation == null
                ? "입력값이 올바르지 않습니다."
                : humanizeValidationMessage(extractLeafProperty(violation), violation.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
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
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
    }

    private String humanizeValidationMessage(String fieldName, String rawMessage) {
        String label = FIELD_LABELS.getOrDefault(fieldName, fieldName);
        if (rawMessage == null || rawMessage.isBlank()) {
            return label + " 입력값을 확인해주세요.";
        }
        String message = rawMessage.replace(fieldName, label);
        for (Map.Entry<String, String> entry : FIELD_LABELS.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }

    private String extractLeafProperty(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }
}
