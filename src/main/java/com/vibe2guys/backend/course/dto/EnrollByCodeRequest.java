package com.vibe2guys.backend.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnrollByCodeRequest(
        @NotBlank(message = "courseCode는 필수입니다.")
        @Size(max = 20, message = "courseCode는 20자 이하여야 합니다.")
        String courseCode
) {
}
