package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateContentRequest(
        @NotNull(message = "type은 필수입니다.")
        ContentType type,
        @NotBlank(message = "title은 필수입니다.")
        @Size(max = 200, message = "title은 200자 이하여야 합니다.")
        String title,
        @NotBlank(message = "description은 필수입니다.")
        @Size(max = 2000, message = "description은 2000자 이하여야 합니다.")
        String description,
        @Size(max = 500, message = "videoUrl은 500자 이하여야 합니다.")
        String videoUrl,
        @Size(max = 500, message = "documentUrl은 500자 이하여야 합니다.")
        String documentUrl,
        Integer durationSeconds,
        OffsetDateTime scheduledAt,
        @NotNull(message = "openAt은 필수입니다.")
        OffsetDateTime openAt
) {
}
