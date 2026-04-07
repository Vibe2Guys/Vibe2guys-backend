package com.vibe2guys.backend.analytics.dto;

import java.util.List;

public record StudentAiUnderstandingResponse(
        Long studentId,
        String studentName,
        int understandingScore,
        String summary,
        List<String> strengths,
        List<String> gaps,
        List<FollowUpUnderstandingItemResponse> followUps
) {
}
