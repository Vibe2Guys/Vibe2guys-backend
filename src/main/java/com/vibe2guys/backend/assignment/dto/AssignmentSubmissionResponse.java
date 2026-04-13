package com.vibe2guys.backend.assignment.dto;

import com.vibe2guys.backend.assignment.domain.AssignmentSubmission;

import java.time.OffsetDateTime;

public record AssignmentSubmissionResponse(
        Long submissionId,
        OffsetDateTime submittedAt,
        String status,
        Integer score,
        String feedback,
        OffsetDateTime gradedAt
) {
    public static AssignmentSubmissionResponse from(AssignmentSubmission submission) {
        return new AssignmentSubmissionResponse(
                submission.getId(),
                submission.getSubmittedAt(),
                submission.getStatus().name(),
                submission.getScore(),
                submission.getFeedbackText(),
                submission.getGradedAt()
        );
    }
}
