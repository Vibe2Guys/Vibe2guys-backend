package com.vibe2guys.backend.team.dto;

import com.vibe2guys.backend.team.domain.TeamTaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTeamTaskStatusRequest(
        @NotNull(message = "업무 상태는 필수입니다.")
        TeamTaskStatus status
) {
}
