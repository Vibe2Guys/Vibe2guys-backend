package com.vibe2guys.backend.course.dto;

public record EnrollmentResponse(
        Long courseId,
        Long userId,
        String status
) {
}
