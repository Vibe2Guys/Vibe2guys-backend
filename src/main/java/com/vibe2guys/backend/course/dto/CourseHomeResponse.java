package com.vibe2guys.backend.course.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record CourseHomeResponse(
        Long courseId,
        String title,
        String description,
        String courseCode,
        boolean isPublic,
        String instructorName,
        int progressRate,
        int attendanceRate,
        int pendingTaskCount,
        String recentLearningTitle,
        OffsetDateTime recentLearningAt,
        List<CourseAnnouncementResponse> announcements,
        List<CourseHomeTodoItemResponse> todos,
        List<CourseWeekSummaryResponse> weeks
) {
}
