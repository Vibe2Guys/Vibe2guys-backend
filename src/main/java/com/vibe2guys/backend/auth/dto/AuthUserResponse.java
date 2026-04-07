package com.vibe2guys.backend.auth.dto;

import com.vibe2guys.backend.user.domain.User;

public record AuthUserResponse(
        Long userId,
        String name,
        String email,
        String role
) {
    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
