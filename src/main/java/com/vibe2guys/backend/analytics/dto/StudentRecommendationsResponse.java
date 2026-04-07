package com.vibe2guys.backend.analytics.dto;

import java.util.List;

public record StudentRecommendationsResponse(
        List<String> recommendations
) {
}
