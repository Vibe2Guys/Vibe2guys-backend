package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.CourseEnrollment;

import java.time.OffsetDateTime;

public record CourseStudentItemResponse(
        Long userId,
        String name,
        String email,
        String status,
        OffsetDateTime enrolledAt
) {
    public static CourseStudentItemResponse from(CourseEnrollment enrollment) {
        return new CourseStudentItemResponse(
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.getStudent().getEmail(),
                enrollment.getStatus().name(),
                enrollment.getEnrolledAt()
        );
    }
}
