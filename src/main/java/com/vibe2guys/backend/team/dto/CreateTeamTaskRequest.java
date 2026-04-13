package com.vibe2guys.backend.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateTeamTaskRequest(
        @NotBlank(message = "업무 제목은 필수입니다.")
        @Size(max = 200, message = "업무 제목은 200자 이하여야 합니다.")
        String title,
        @Size(max = 2000, message = "업무 설명은 2000자 이하여야 합니다.")
        String description,
        Long assigneeUserId,
        OffsetDateTime dueAt
) {
}
