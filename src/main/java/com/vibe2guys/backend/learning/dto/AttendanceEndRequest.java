package com.vibe2guys.backend.learning.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record AttendanceEndRequest(
        @NotNull(message = "leftAt은 필수입니다.")
        OffsetDateTime leftAt
) {
}
