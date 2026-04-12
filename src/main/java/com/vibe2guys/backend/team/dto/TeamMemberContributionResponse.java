package com.vibe2guys.backend.team.dto;

public record TeamMemberContributionResponse(
        Long userId,
        String name,
        String learningStyle,
        int reliabilityScore,
        int messageCount,
        int contributionScore
) {
}
