package com.vibe2guys.backend.analytics.dto;

import com.vibe2guys.backend.analytics.domain.DailyAnalyticsSnapshot;

import java.util.List;

public record StudentScoresResponse(
        int learningSincerityScore,
        int understandingScore,
        int engagementScore,
        int collaborationScore,
        int riskScore,
        String riskLevel,
        List<String> reasons
) {
    public static StudentScoresResponse from(DailyAnalyticsSnapshot snapshot) {
        return new StudentScoresResponse(
                snapshot.getDiligenceScore(),
                snapshot.getUnderstandingScore(),
                snapshot.getEngagementScore(),
                snapshot.getCollaborationScore(),
                snapshot.getDropoutRiskScore(),
                snapshot.getRiskLevel().name(),
                snapshot.getReasons()
        );
    }
}
