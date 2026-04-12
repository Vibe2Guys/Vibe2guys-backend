package com.vibe2guys.backend.team.dto;

import java.util.List;

public record AutoGroupingResponse(
        int teamCount,
        String groupingBasis,
        List<TeamListItemResponse> teams
) {
}
