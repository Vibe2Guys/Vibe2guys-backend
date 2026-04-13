package com.vibe2guys.backend.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCourseAnnouncementRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,
        @NotBlank(message = "공지 내용은 필수입니다.")
        @Size(max = 4000, message = "공지 내용은 4000자 이하여야 합니다.")
        String body,
        boolean pinned
) {
}
