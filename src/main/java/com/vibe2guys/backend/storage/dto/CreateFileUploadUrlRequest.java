package com.vibe2guys.backend.storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFileUploadUrlRequest(
        @NotBlank(message = "파일 이름은 필수입니다.")
        @Size(max = 255, message = "파일 이름은 255자 이하여야 합니다.")
        String fileName,
        @NotBlank(message = "파일 형식은 필수입니다.")
        @Size(max = 100, message = "파일 형식은 100자 이하여야 합니다.")
        String contentType,
        @NotNull(message = "업로드 구분은 필수입니다.")
        FileUploadCategory category
) {
}
