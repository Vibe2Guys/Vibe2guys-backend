package com.vibe2guys.backend.quiz.dto;

import com.vibe2guys.backend.quiz.domain.Quiz;

import java.time.OffsetDateTime;
import java.util.List;

public record QuizDetailResponse(
        Long quizId,
        String title,
        OffsetDateTime dueAt,
        List<QuizQuestionResponse> questions
) {
    public static QuizDetailResponse of(Quiz quiz, List<QuizQuestionResponse> questions) {
        return new QuizDetailResponse(quiz.getId(), quiz.getTitle(), quiz.getDueAt(), questions);
    }
}
