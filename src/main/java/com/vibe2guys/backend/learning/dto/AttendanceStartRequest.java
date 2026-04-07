package com.vibe2guys.backend.learning.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record AttendanceStartRequest(
        @NotNull(message = "enteredAt은 필수입니다.")
        OffsetDateTime enteredAt
) {
}
