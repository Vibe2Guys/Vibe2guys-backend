package com.vibe2guys.backend.assignment.dto;

import com.vibe2guys.backend.assignment.domain.Assignment;

import java.time.OffsetDateTime;

public record AssignmentListItemResponse(
        Long assignmentId,
        String title,
        String type,
        OffsetDateTime dueAt,
        int maxScore,
        boolean isSubmitted,
        boolean teamAssignment
) {
    public static AssignmentListItemResponse of(Assignment assignment, boolean submitted) {
        return new AssignmentListItemResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getType().name(),
                assignment.getDueAt(),
                assignment.getMaxScore(),
                submitted,
                assignment.isTeamAssignment()
        );
    }
}
