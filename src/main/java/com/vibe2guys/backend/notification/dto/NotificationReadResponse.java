package com.vibe2guys.backend.notification.dto;

import com.vibe2guys.backend.notification.domain.Notification;

import java.time.OffsetDateTime;

public record NotificationReadResponse(
        Long notificationId,
        boolean isRead,
        OffsetDateTime readAt
) {

    public static NotificationReadResponse from(Notification notification) {
        return new NotificationReadResponse(notification.getId(), notification.isRead(), notification.getReadAt());
    }
}
