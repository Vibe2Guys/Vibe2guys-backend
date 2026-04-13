package com.vibe2guys.backend.assignment.dto;

import com.vibe2guys.backend.assignment.domain.AssignmentSubmission;
import com.vibe2guys.backend.assignment.domain.AssignmentSubmissionStatus;

import java.time.OffsetDateTime;

public record AssignmentSubmissionSummaryResponse(
        Long submissionId,
        String status,
        OffsetDateTime submittedAt,
        Integer score,
        String feedback,
        OffsetDateTime gradedAt
) {
    public static AssignmentSubmissionSummaryResponse from(AssignmentSubmission submission) {
        return new AssignmentSubmissionSummaryResponse(
                submission.getId(),
                submission.getStatus().name(),
                submission.getSubmittedAt(),
                submission.getScore(),
                submission.getFeedbackText(),
                submission.getGradedAt()
        );
    }

    public static AssignmentSubmissionSummaryResponse notSubmitted() {
        return new AssignmentSubmissionSummaryResponse(
                null,
                AssignmentSubmissionStatus.NOT_SUBMITTED.name(),
                null,
                null,
                null,
                null
        );
    }
}
