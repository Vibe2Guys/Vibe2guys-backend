package com.vibe2guys.backend.assignment.dto;

import com.vibe2guys.backend.assignment.domain.AssignmentSubmission;

import java.time.OffsetDateTime;

public record AssignmentSubmissionListItemResponse(
        Long submissionId,
        Long studentId,
        String studentName,
        String status,
        OffsetDateTime submittedAt,
        Integer score,
        String feedback,
        OffsetDateTime gradedAt
) {
    public static AssignmentSubmissionListItemResponse from(AssignmentSubmission submission) {
        return new AssignmentSubmissionListItemResponse(
                submission.getId(),
                submission.getStudent().getId(),
                submission.getStudent().getName(),
                submission.getStatus().name(),
                submission.getSubmittedAt(),
                submission.getScore(),
                submission.getFeedbackText(),
                submission.getGradedAt()
        );
    }
}
