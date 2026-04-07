package com.vibe2guys.backend.notification.dto;

import com.vibe2guys.backend.notification.domain.Notification;

import java.time.OffsetDateTime;

public record NotificationItemResponse(
        Long notificationId,
        String type,
        String title,
        String content,
        boolean isRead,
        OffsetDateTime createdAt,
        OffsetDateTime readAt
) {

    public static NotificationItemResponse from(Notification notification) {
        return new NotificationItemResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
