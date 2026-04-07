package com.vibe2guys.backend.ai.dto;

import com.vibe2guys.backend.ai.domain.AiFollowUpAnalysis;

public record FollowUpAnalysisResponse(
        int understandingScore,
        String feedback,
        int responseDelaySeconds
) {
    public static FollowUpAnalysisResponse of(AiFollowUpAnalysis analysis) {
        return new FollowUpAnalysisResponse(
                analysis.getUnderstandingScore(),
                analysis.getFeedback(),
                analysis.getResponse().getResponseDelaySeconds()
        );
    }
}
