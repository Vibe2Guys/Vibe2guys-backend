package com.vibe2guys.backend.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateWeekRequest(
        @Min(value = 1, message = "weekNumber는 1 이상이어야 합니다.")
        int weekNumber,
        @NotBlank(message = "title은 필수입니다.")
        @Size(max = 200, message = "title은 200자 이하여야 합니다.")
        String title,
        @NotNull(message = "openAt은 필수입니다.")
        OffsetDateTime openAt
) {
}
