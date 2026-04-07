package com.vibe2guys.backend.analytics.dto;

import java.util.List;

public record StudentDashboardResponse(
        int attendanceRate,
        int progressRate,
        int assignmentSubmitRate,
        int understandingScore,
        int engagementScore,
        String riskLevel,
        String coachingMessage,
        List<String> todayTodos,
        List<StudentCourseAnalyticsItemResponse> courses
) {
}
