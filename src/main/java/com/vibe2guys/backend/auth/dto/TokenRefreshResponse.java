package com.vibe2guys.backend.auth.dto;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken
) {
}
