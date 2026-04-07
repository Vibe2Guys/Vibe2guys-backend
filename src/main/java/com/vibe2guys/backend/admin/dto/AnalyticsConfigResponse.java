package com.vibe2guys.backend.admin.dto;

import com.vibe2guys.backend.admin.domain.AnalyticsConfig;

public record AnalyticsConfigResponse(
        double attendanceWeight,
        double progressWeight,
        double assignmentWeight,
        double quizWeight,
        double teamActivityWeight,
        int riskThresholdHigh,
        int riskThresholdMedium
) {
    public static AnalyticsConfigResponse from(AnalyticsConfig config) {
        return new AnalyticsConfigResponse(
                config.getAttendanceWeight(),
                config.getProgressWeight(),
                config.getAssignmentWeight(),
                config.getQuizWeight(),
                config.getTeamActivityWeight(),
                config.getRiskThresholdHigh(),
                config.getRiskThresholdMedium()
        );
    }
}
