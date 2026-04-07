package com.vibe2guys.backend.team.dto;

public record TeamMemberContributionResponse(
        Long userId,
        String name,
        int messageCount,
        int contributionScore
) {
}
