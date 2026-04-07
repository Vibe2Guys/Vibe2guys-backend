package com.vibe2guys.backend.quiz.dto;

import com.vibe2guys.backend.quiz.domain.QuizQuestion;

import java.util.List;

public record QuizQuestionResponse(
        Long questionId,
        String questionType,
        String questionText,
        List<String> choices,
        int score
) {
    public static QuizQuestionResponse from(QuizQuestion question) {
        return new QuizQuestionResponse(
                question.getId(),
                question.getQuestionType().name(),
                question.getQuestionText(),
                question.getChoicesJson(),
                question.getScore()
        );
    }
}
