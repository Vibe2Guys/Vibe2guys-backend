package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.CourseEnrollment;

import java.time.OffsetDateTime;

public record CourseStudentItemResponse(
        Long userId,
        String name,
        String email,
        String status,
        OffsetDateTime enrolledAt,
        int progressRate,
        int attendanceRate,
        int understandingScore,
        String riskLevel,
        String statusSummary,
        String memo
) {
    public static CourseStudentItemResponse of(
            CourseEnrollment enrollment,
            int progressRate,
            int attendanceRate,
            int understandingScore,
            String riskLevel,
            String statusSummary,
            String memo
    ) {
        return new CourseStudentItemResponse(
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.getStudent().getEmail(),
                enrollment.getStatus().name(),
                enrollment.getEnrolledAt(),
                progressRate,
                attendanceRate,
                understandingScore,
                riskLevel,
                statusSummary,
                memo
        );
    }
}
