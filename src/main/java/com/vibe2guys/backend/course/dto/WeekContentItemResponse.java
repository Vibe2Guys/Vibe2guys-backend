package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.Content;

import java.time.OffsetDateTime;

public record WeekContentItemResponse(
        Long contentId,
        String type,
        String title,
        String description,
        OffsetDateTime openAt,
        boolean isPublished
) {
    public static WeekContentItemResponse from(Content content) {
        return new WeekContentItemResponse(
                content.getId(),
                content.getType().name(),
                content.getTitle(),
                content.getDescription(),
                content.getOpenAt(),
                content.isPublished()
        );
    }
}
