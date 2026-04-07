package com.vibe2guys.backend.notification.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.notification.dto.NotificationItemResponse;
import com.vibe2guys.backend.notification.dto.NotificationReadResponse;
import com.vibe2guys.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public ApiResponse<List<NotificationItemResponse>> getMyNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("알림 조회 성공", notificationService.getMyNotifications(principal.getId()));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationReadResponse> readNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("알림 읽음 처리 완료", notificationService.readNotification(notificationId, principal.getId()));
    }
}
