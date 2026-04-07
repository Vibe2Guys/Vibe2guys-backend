package com.vibe2guys.backend.analytics.dto;

import com.vibe2guys.backend.analytics.domain.DailyAnalyticsSnapshot;

public record StudentCourseAnalyticsItemResponse(
        Long courseId,
        String courseTitle,
        int diligenceScore,
        int understandingScore,
        int engagementScore,
        int collaborationScore,
        int riskScore,
        String riskLevel
) {
    public static StudentCourseAnalyticsItemResponse from(DailyAnalyticsSnapshot snapshot) {
        return new StudentCourseAnalyticsItemResponse(
                snapshot.getCourse().getId(),
                snapshot.getCourse().getTitle(),
                snapshot.getDiligenceScore(),
                snapshot.getUnderstandingScore(),
                snapshot.getEngagementScore(),
                snapshot.getCollaborationScore(),
                snapshot.getDropoutRiskScore(),
                snapshot.getRiskLevel().name()
        );
    }
}
