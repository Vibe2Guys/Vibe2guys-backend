package com.vibe2guys.backend.team.dto;

import com.vibe2guys.backend.team.domain.Team;

public record TeamListItemResponse(
        Long teamId,
        String name,
        int memberCount,
        String status,
        int teamBuildingScore,
        String matchingSummary
) {
    public static TeamListItemResponse of(Team team, int memberCount) {
        return new TeamListItemResponse(
                team.getId(),
                team.getName(),
                memberCount,
                team.getStatus().name(),
                team.getTeamBuildingScore(),
                team.getMatchingSummary()
        );
    }
}
