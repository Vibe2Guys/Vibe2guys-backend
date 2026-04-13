package com.vibe2guys.backend.team.dto;

import com.vibe2guys.backend.team.domain.TeamTask;

import java.time.OffsetDateTime;

public record TeamTaskResponse(
        Long taskId,
        String title,
        String description,
        String status,
        Long assigneeUserId,
        String assigneeName,
        OffsetDateTime dueAt,
        String createdByName
) {
    public static TeamTaskResponse from(TeamTask task) {
        return new TeamTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getAssignee() != null ? task.getAssignee().getName() : null,
                task.getDueAt(),
                task.getCreatedBy().getName()
        );
    }
}
