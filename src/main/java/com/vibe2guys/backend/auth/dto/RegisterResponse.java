package com.vibe2guys.backend.auth.dto;

import com.vibe2guys.backend.user.domain.User;

public record RegisterResponse(
        Long userId,
        String name,
        String email,
        String role
) {
    public static RegisterResponse from(User user) {
        return new RegisterResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
