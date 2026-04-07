package com.vibe2guys.backend.quiz.dto;

import com.vibe2guys.backend.quiz.domain.QuizSubmission;

public record QuizSubmissionResponse(
        Long quizSubmissionId,
        int objectiveScore,
        boolean subjectivePending,
        String status
) {
    public static QuizSubmissionResponse from(QuizSubmission submission) {
        return new QuizSubmissionResponse(
                submission.getId(),
                submission.getObjectiveScore(),
                submission.getSubjectiveScore() == null,
                submission.getStatus().name()
        );
    }
}
