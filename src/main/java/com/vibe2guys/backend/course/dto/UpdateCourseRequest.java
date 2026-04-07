package com.vibe2guys.backend.course.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateCourseRequest(
        @Size(max = 200, message = "title은 200자 이하여야 합니다.")
        String title,
        @Size(max = 2000, message = "description은 2000자 이하여야 합니다.")
        String description,
        @Size(max = 500, message = "thumbnailUrl은 500자 이하여야 합니다.")
        String thumbnailUrl,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isSequentialRelease,
        String status
) {
}
