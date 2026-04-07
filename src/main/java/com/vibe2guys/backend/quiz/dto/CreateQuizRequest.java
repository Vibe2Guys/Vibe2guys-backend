package com.vibe2guys.backend.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateQuizRequest(
        @NotBlank(message = "title은 필수입니다.")
        @Size(max = 200, message = "title은 200자 이하여야 합니다.")
        String title,
        @NotNull(message = "dueAt은 필수입니다.")
        OffsetDateTime dueAt,
        @NotEmpty(message = "questions는 비어 있을 수 없습니다.")
        List<@Valid CreateQuizQuestionRequest> questions
) {
}
