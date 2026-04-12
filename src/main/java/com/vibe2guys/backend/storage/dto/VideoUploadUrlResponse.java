package com.vibe2guys.backend.storage.dto;

public record VideoUploadUrlResponse(
        String uploadUrl,
        String fileUrl,
        String objectKey,
        long expiresInSeconds
) {
}
