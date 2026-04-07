package com.vibe2guys.backend.course.dto;

public record MyCourseItemResponse(
        Long courseId,
        String title,
        String description,
        String thumbnailUrl,
        String instructorName,
        int progressRate,
        int attendanceRate,
        int assignmentPendingCount
) {
}
