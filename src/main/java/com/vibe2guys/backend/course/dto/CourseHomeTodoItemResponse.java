package com.vibe2guys.backend.course.dto;

import java.time.OffsetDateTime;

public record CourseHomeTodoItemResponse(
        String category,
        Long referenceId,
        String title,
        String status,
        OffsetDateTime scheduleAt,
        String summary
) {
}
