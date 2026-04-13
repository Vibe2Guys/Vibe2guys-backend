package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.CourseAnnouncement;

import java.time.OffsetDateTime;

public record CourseAnnouncementResponse(
        Long announcementId,
        String title,
        String body,
        boolean pinned,
        String createdByName,
        OffsetDateTime createdAt
) {
    public static CourseAnnouncementResponse from(CourseAnnouncement announcement) {
        return new CourseAnnouncementResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getBody(),
                announcement.isPinned(),
                announcement.getCreatedBy().getName(),
                announcement.getCreatedAt()
        );
    }
}
