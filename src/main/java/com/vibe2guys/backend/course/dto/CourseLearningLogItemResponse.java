package com.vibe2guys.backend.course.dto;

public record CourseLearningLogItemResponse(
        Long contentId,
        String title,
        String type,
        Integer progressRate,
        Boolean isCompleted,
        String attendanceStatus,
        Integer attendanceMinutes
) {
}
