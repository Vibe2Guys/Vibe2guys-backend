package com.vibe2guys.backend.analytics.dto;

import com.vibe2guys.backend.analytics.domain.DailyAnalyticsSnapshot;

import java.util.List;

public record StudentRiskResponse(
        int riskScore,
        String riskLevel,
        List<String> reasons
) {
    public static StudentRiskResponse from(DailyAnalyticsSnapshot snapshot) {
        return new StudentRiskResponse(
                snapshot.getDropoutRiskScore(),
                snapshot.getRiskLevel().name(),
                snapshot.getReasons()
        );
    }
}
