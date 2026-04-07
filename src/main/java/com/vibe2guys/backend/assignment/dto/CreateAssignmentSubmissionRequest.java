package com.vibe2guys.backend.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateAssignmentSubmissionRequest(
        @NotBlank(message = "answerText는 필수입니다.")
        @Size(max = 10000, message = "answerText는 10000자 이하여야 합니다.")
        String answerText,
        List<@Size(max = 500, message = "fileUrl은 500자 이하여야 합니다.") String> fileUrls
) {
}
