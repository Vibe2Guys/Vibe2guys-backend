package com.vibe2guys.backend.ai.service;

import com.vibe2guys.backend.analytics.domain.RiskLevel;

import java.util.List;

public record AiRiskAssessment(
        int riskScore,
        RiskLevel riskLevel,
        List<String> reasons,
        String coachingMessage,
        List<String> recommendations
) {
}
