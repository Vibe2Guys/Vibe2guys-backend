package com.vibe2guys.backend.course.dto;

import jakarta.validation.constraints.Size;

public record UpdateCourseStudentMemoRequest(
        @Size(max = 2000, message = "메모는 2000자 이하여야 합니다.")
        String memo
) {
}
