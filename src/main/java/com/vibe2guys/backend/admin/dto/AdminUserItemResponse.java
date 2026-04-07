package com.vibe2guys.backend.admin.dto;

import com.vibe2guys.backend.user.domain.User;

public record AdminUserItemResponse(
        Long userId,
        String name,
        String email,
        String role,
        String status
) {
    public static AdminUserItemResponse from(User user) {
        return new AdminUserItemResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }
}
