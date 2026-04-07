package com.vibe2guys.backend.ai.dto;

import com.vibe2guys.backend.ai.domain.AiFollowUpQuestion;

public record FollowUpQuestionResponse(
        Long questionId,
        String questionText,
        String difficultyLevel
) {
    public static FollowUpQuestionResponse from(AiFollowUpQuestion question) {
        return new FollowUpQuestionResponse(question.getId(), question.getQuestionText(), question.getDifficultyLevel().name());
    }
}
