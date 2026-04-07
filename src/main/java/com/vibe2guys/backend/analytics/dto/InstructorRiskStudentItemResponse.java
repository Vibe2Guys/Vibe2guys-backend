package com.vibe2guys.backend.analytics.dto;

import com.vibe2guys.backend.analytics.domain.DailyAnalyticsSnapshot;

import java.util.List;

public record InstructorRiskStudentItemResponse(
        Long studentId,
        String studentName,
        int riskScore,
        String riskLevel,
        List<String> reasons
) {
    public static InstructorRiskStudentItemResponse from(DailyAnalyticsSnapshot snapshot) {
        return new InstructorRiskStudentItemResponse(
                snapshot.getStudent().getId(),
                snapshot.getStudent().getName(),
                snapshot.getDropoutRiskScore(),
                snapshot.getRiskLevel().name(),
                snapshot.getReasons()
        );
    }
}
