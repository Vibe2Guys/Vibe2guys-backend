package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.Content;

public record ContentDetailResponse(
        Long contentId,
        String type,
        String title,
        String description,
        String videoUrl,
        Integer durationSeconds
) {
    public static ContentDetailResponse from(Content content) {
        return new ContentDetailResponse(
                content.getId(),
                content.getType().name(),
                content.getTitle(),
                content.getDescription(),
                content.getVideoUrl(),
                content.getDurationSeconds()
        );
    }
}
