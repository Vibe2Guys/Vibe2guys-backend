package com.vibe2guys.backend.analytics.dto;

import com.vibe2guys.backend.ai.domain.AiFollowUpAnalysis;

import java.time.OffsetDateTime;

public record FollowUpUnderstandingItemResponse(
        Long questionId,
        String contextType,
        String questionText,
        int understandingScore,
        String feedback,
        OffsetDateTime analyzedAt
) {

    public static FollowUpUnderstandingItemResponse from(AiFollowUpAnalysis analysis) {
        return new FollowUpUnderstandingItemResponse(
                analysis.getQuestion().getId(),
                analysis.getQuestion().getContextType().name(),
                analysis.getQuestion().getQuestionText(),
                analysis.getUnderstandingScore(),
                analysis.getFeedback(),
                analysis.getAnalyzedAt()
        );
    }
}
