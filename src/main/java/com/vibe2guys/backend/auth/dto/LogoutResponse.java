package com.vibe2guys.backend.auth.dto;

import java.time.OffsetDateTime;

public record LogoutResponse(
        boolean revoked,
        OffsetDateTime revokedAt
) {
}
