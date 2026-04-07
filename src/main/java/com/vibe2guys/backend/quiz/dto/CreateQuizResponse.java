package com.vibe2guys.backend.quiz.dto;

import com.vibe2guys.backend.quiz.domain.Quiz;

public record CreateQuizResponse(
        Long quizId,
        String title,
        int questionCount
) {
    public static CreateQuizResponse of(Quiz quiz, int questionCount) {
        return new CreateQuizResponse(quiz.getId(), quiz.getTitle(), questionCount);
    }
}
