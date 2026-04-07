package com.vibe2guys.backend.analytics.dto;

public record ScoreDistributionBucketResponse(
        String range,
        int count
) {
}
