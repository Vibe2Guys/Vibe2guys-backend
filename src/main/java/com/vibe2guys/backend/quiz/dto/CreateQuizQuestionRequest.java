package com.vibe2guys.backend.quiz.dto;

import com.vibe2guys.backend.quiz.domain.QuizQuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateQuizQuestionRequest(
        @NotNull(message = "questionType은 필수입니다.")
        QuizQuestionType questionType,
        @NotBlank(message = "questionText는 필수입니다.")
        @Size(max = 2000, message = "questionText는 2000자 이하여야 합니다.")
        String questionText,
        List<@Size(max = 500, message = "choice는 500자 이하여야 합니다.") String> choices,
        @Size(max = 2000, message = "answerKey는 2000자 이하여야 합니다.")
        String answerKey,
        @Min(value = 1, message = "score는 1 이상이어야 합니다.")
        int score,
        @Min(value = 1, message = "sortOrder는 1 이상이어야 합니다.")
        int sortOrder
) {
}
