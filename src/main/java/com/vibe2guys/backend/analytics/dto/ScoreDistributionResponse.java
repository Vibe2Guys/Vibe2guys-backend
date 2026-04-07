package com.vibe2guys.backend.analytics.dto;

import java.util.List;

public record ScoreDistributionResponse(
        Long courseId,
        String courseTitle,
        List<ScoreDistributionBucketResponse> understandingScoreDistribution,
        List<ScoreDistributionBucketResponse> riskScoreDistribution
) {
}
