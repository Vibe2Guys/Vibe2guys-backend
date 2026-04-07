package com.vibe2guys.backend.analytics.dto;

import com.vibe2guys.backend.assignment.domain.Assignment;
import com.vibe2guys.backend.assignment.domain.AssignmentSubmission;

import java.time.OffsetDateTime;

public record StudentAssignmentProgressItemResponse(
        Long assignmentId,
        String title,
        OffsetDateTime dueAt,
        String submissionStatus,
        OffsetDateTime submittedAt
) {
    public static StudentAssignmentProgressItemResponse of(Assignment assignment, AssignmentSubmission submission) {
        return new StudentAssignmentProgressItemResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDueAt(),
                submission == null ? "NOT_SUBMITTED" : submission.getStatus().name(),
                submission == null ? null : submission.getSubmittedAt()
        );
    }
}
