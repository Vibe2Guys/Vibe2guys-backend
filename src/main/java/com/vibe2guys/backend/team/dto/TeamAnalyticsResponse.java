package com.vibe2guys.backend.team.dto;

import java.util.List;

public record TeamAnalyticsResponse(
        int teamBuildingScore,
        int profileDiversityScore,
        String matchingSummary,
        int collaborationScore,
        int conversationBalanceScore,
        int inactiveMemberCount,
        int dominantMemberCount,
        List<String> riskSignals,
        List<String> strengthSignals,
        List<TeamStyleDistributionResponse> styleDistributions
) {
}
