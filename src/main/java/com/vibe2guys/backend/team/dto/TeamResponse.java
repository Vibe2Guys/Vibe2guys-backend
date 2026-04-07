package com.vibe2guys.backend.team.dto;

import java.util.List;

public record TeamResponse(
        Long teamId,
        Long courseId,
        String name,
        String status,
        int collaborationScore,
        int conversationBalanceScore,
        int inactiveMemberCount,
        int dominantMemberCount,
        List<String> riskSignals,
        List<TeamMemberResponse> members
) {
}
