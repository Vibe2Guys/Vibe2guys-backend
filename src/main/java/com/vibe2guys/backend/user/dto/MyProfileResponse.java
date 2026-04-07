package com.vibe2guys.backend.user.dto;

import com.vibe2guys.backend.user.domain.User;

public record MyProfileResponse(
        Long userId,
        String name,
        String email,
        String role,
        String profileImageUrl
) {
    public static MyProfileResponse from(User user) {
        return new MyProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getProfileImageUrl()
        );
    }
}
