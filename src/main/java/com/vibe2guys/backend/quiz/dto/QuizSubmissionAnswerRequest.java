package com.vibe2guys.backend.quiz.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuizSubmissionAnswerRequest(
        @NotNull(message = "questionId는 필수입니다.")
        Long questionId,
        @Size(max = 2000, message = "selectedChoice는 2000자 이하여야 합니다.")
        String selectedChoice,
        @Size(max = 10000, message = "answerText는 10000자 이하여야 합니다.")
        String answerText
) {
}
