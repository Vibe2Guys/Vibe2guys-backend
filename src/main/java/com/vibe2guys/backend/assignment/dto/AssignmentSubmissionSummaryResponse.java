package com.vibe2guys.backend.assignment.dto;

import com.vibe2guys.backend.assignment.domain.AssignmentSubmission;
import com.vibe2guys.backend.assignment.domain.AssignmentSubmissionStatus;

import java.time.OffsetDateTime;

public record AssignmentSubmissionSummaryResponse(
        Long submissionId,
        String status,
        OffsetDateTime submittedAt
) {
    public static AssignmentSubmissionSummaryResponse from(AssignmentSubmission submission) {
        return new AssignmentSubmissionSummaryResponse(
                submission.getId(),
                submission.getStatus().name(),
                submission.getSubmittedAt()
        );
    }

    public static AssignmentSubmissionSummaryResponse notSubmitted() {
        return new AssignmentSubmissionSummaryResponse(null, AssignmentSubmissionStatus.NOT_SUBMITTED.name(), null);
    }
}
