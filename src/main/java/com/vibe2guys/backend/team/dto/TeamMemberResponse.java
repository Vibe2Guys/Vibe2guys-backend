package com.vibe2guys.backend.team.dto;

import com.vibe2guys.backend.team.domain.TeamMember;

public record TeamMemberResponse(
        Long userId,
        String name,
        String email,
        String status,
        String learningStyle,
        int reliabilityScore,
        int initiativeScore,
        int supportScore,
        int understandingScore,
        String profileSummary
) {
    public static TeamMemberResponse from(TeamMember teamMember) {
        return new TeamMemberResponse(
                teamMember.getUser().getId(),
                teamMember.getUser().getName(),
                teamMember.getUser().getEmail(),
                teamMember.getStatus().name(),
                teamMember.getLearningStyle().name(),
                teamMember.getReliabilityScore(),
                teamMember.getInitiativeScore(),
                teamMember.getSupportScore(),
                teamMember.getUnderstandingScore(),
                teamMember.getProfileSummary()
        );
    }
}
