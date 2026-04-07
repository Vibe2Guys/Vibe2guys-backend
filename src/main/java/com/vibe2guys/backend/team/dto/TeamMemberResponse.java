package com.vibe2guys.backend.team.dto;

import com.vibe2guys.backend.team.domain.TeamMember;

public record TeamMemberResponse(
        Long userId,
        String name,
        String email,
        String status
) {
    public static TeamMemberResponse from(TeamMember teamMember) {
        return new TeamMemberResponse(
                teamMember.getUser().getId(),
                teamMember.getUser().getName(),
                teamMember.getUser().getEmail(),
                teamMember.getStatus().name()
        );
    }
}
