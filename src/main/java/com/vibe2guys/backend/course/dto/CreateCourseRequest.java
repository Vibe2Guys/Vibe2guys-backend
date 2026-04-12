package com.vibe2guys.backend.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCourseRequest(
        @NotBlank(message = "title은 필수입니다.")
        @Size(max = 200, message = "title은 200자 이하여야 합니다.")
        String title,
        @NotBlank(message = "description은 필수입니다.")
        @Size(max = 2000, message = "description은 2000자 이하여야 합니다.")
        String description,
        @Size(max = 500, message = "thumbnailUrl은 500자 이하여야 합니다.")
        String thumbnailUrl,
        @NotNull(message = "startDate는 필수입니다.")
        LocalDate startDate,
        @NotNull(message = "endDate는 필수입니다.")
        LocalDate endDate,
        boolean isSequentialRelease,
        boolean isPublic
) {
}
