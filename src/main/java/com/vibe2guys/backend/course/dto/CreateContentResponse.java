package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.Content;

public record CreateContentResponse(
        Long contentId,
        String title
) {
    public static CreateContentResponse from(Content content) {
        return new CreateContentResponse(content.getId(), content.getTitle());
    }
}
