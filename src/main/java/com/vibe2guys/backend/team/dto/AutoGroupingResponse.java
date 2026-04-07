package com.vibe2guys.backend.team.dto;

import java.util.List;

public record AutoGroupingResponse(
        int teamCount,
        List<TeamListItemResponse> teams
) {
}
