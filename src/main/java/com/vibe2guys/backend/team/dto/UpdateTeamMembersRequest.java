package com.vibe2guys.backend.team.dto;

import java.util.List;

public record UpdateTeamMembersRequest(
        List<Long> removeMemberIds,
        List<Long> addMemberIds
) {
}
