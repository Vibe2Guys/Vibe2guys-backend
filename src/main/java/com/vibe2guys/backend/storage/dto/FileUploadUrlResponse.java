package com.vibe2guys.backend.storage.dto;

public record FileUploadUrlResponse(
        String uploadUrl,
        String fileUrl,
        String objectKey,
        long expiresInSeconds
) {
}
