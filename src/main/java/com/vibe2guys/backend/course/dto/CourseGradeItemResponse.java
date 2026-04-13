package com.vibe2guys.backend.course.dto;

import java.time.OffsetDateTime;

public record CourseGradeItemResponse(
        String category,
        Long referenceId,
        String title,
        int earnedScore,
        int maxScore,
        int percentScore,
        String status,
        String feedback,
        OffsetDateTime submittedAt,
        OffsetDateTime dueAt
) {
}
