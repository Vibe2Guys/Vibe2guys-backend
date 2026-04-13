package com.vibe2guys.backend.assignment.dto;

import com.vibe2guys.backend.assignment.domain.Assignment;

import java.time.OffsetDateTime;

public record AssignmentDetailResponse(
        Long assignmentId,
        String title,
        String description,
        String type,
        OffsetDateTime dueAt,
        int maxScore,
        boolean teamAssignment,
        AssignmentSubmissionSummaryResponse mySubmission
) {
    public static AssignmentDetailResponse of(Assignment assignment, AssignmentSubmissionSummaryResponse mySubmission) {
        return new AssignmentDetailResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getType().name(),
                assignment.getDueAt(),
                assignment.getMaxScore(),
                assignment.isTeamAssignment(),
                mySubmission
        );
    }
}
