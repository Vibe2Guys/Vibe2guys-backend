package com.vibe2guys.backend.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateQuizSubmissionRequest(
        @NotEmpty(message = "answers는 비어 있을 수 없습니다.")
        List<@Valid QuizSubmissionAnswerRequest> answers
) {
}
