package com.vibe2guys.backend.ai.dto;

import com.vibe2guys.backend.ai.domain.AiFollowUpResponse;

public record FollowUpAnswerResponse(
        Long responseId,
        int responseDelaySeconds
) {
    public static FollowUpAnswerResponse from(AiFollowUpResponse response) {
        return new FollowUpAnswerResponse(response.getId(), response.getResponseDelaySeconds());
    }
}
