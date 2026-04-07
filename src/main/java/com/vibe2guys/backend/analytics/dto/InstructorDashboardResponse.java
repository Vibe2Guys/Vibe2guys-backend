package com.vibe2guys.backend.analytics.dto;

import java.util.List;

public record InstructorDashboardResponse(
        Long courseId,
        String courseTitle,
        int averageProgressRate,
        int averageUnderstandingScore,
        int highRiskStudentCount,
        int totalStudentCount,
        List<InstructorRiskStudentItemResponse> riskStudents
) {
}
