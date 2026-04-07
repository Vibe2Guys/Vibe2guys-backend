package com.vibe2guys.backend.analytics.dto;

import com.vibe2guys.backend.quiz.domain.Quiz;
import com.vibe2guys.backend.quiz.domain.QuizSubmission;

import java.time.OffsetDateTime;

public record StudentQuizProgressItemResponse(
        Long quizId,
        String title,
        OffsetDateTime dueAt,
        Integer totalScore,
        String submissionStatus,
        OffsetDateTime submittedAt
) {
    public static StudentQuizProgressItemResponse of(Quiz quiz, QuizSubmission submission) {
        return new StudentQuizProgressItemResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDueAt(),
                submission == null ? null : submission.getTotalScore(),
                submission == null ? "NOT_SUBMITTED" : submission.getStatus().name(),
                submission == null ? null : submission.getSubmittedAt()
        );
    }
}
