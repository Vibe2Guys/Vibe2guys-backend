package com.vibe2guys.backend.learning.dto;

import com.vibe2guys.backend.learning.domain.ContentProgressSummary;

public record ContentProgressResponse(
        Long contentId,
        int progressRate,
        int lastPositionSeconds,
        int replayCount,
        boolean isCompleted
) {
    public static ContentProgressResponse from(ContentProgressSummary summary) {
        return new ContentProgressResponse(
                summary.getContent().getId(),
                summary.getProgressRate(),
                summary.getLastPositionSeconds(),
                summary.getReplayCount(),
                summary.isCompleted()
        );
    }
}
