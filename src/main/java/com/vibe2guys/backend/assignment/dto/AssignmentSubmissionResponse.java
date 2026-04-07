package com.vibe2guys.backend.assignment.dto;

import com.vibe2guys.backend.assignment.domain.AssignmentSubmission;

import java.time.OffsetDateTime;

public record AssignmentSubmissionResponse(
        Long submissionId,
        OffsetDateTime submittedAt,
        String status
) {
    public static AssignmentSubmissionResponse from(AssignmentSubmission submission) {
        return new AssignmentSubmissionResponse(
                submission.getId(),
                submission.getSubmittedAt(),
                submission.getStatus().name()
        );
    }
}
