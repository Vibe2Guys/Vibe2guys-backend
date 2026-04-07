package com.vibe2guys.backend.quiz.dto;

import com.vibe2guys.backend.quiz.domain.Quiz;

import java.time.OffsetDateTime;

public record QuizListItemResponse(
        Long quizId,
        String title,
        OffsetDateTime dueAt,
        boolean isSubmitted
) {
    public static QuizListItemResponse of(Quiz quiz, boolean submitted) {
        return new QuizListItemResponse(quiz.getId(), quiz.getTitle(), quiz.getDueAt(), submitted);
    }
}
