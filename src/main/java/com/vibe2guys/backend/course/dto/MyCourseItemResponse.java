package com.vibe2guys.backend.course.dto;

public record MyCourseItemResponse(
        Long courseId,
        String title,
        String description,
        String thumbnailUrl,
        String instructorName,
        String courseCode,
        boolean isPublic,
        int progressRate,
        int attendanceRate,
        int assignmentPendingCount
) {
}
