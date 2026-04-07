package com.vibe2guys.backend.learning.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.learning.dto.ContentProgressRequest;
import com.vibe2guys.backend.learning.dto.ContentProgressResponse;
import com.vibe2guys.backend.learning.dto.AttendanceEndRequest;
import com.vibe2guys.backend.learning.dto.AttendanceResponse;
import com.vibe2guys.backend.learning.dto.AttendanceStartRequest;
import com.vibe2guys.backend.learning.service.AttendanceService;
import com.vibe2guys.backend.learning.service.ContentProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
public class ContentProgressController {

    private final ContentProgressService contentProgressService;
    private final AttendanceService attendanceService;

    @PostMapping("/{contentId}/progress")
    public ApiResponse<ContentProgressResponse> saveProgress(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ContentProgressRequest request
    ) {
        return ApiResponse.success("진도 저장 완료", contentProgressService.saveProgress(contentId, principal.getId(), request));
    }

    @GetMapping("/{contentId}/progress")
    public ApiResponse<ContentProgressResponse> getProgress(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("진도 조회 성공", contentProgressService.getProgress(contentId, principal.getId()));
    }

    @PostMapping("/{contentId}/attendance")
    public ApiResponse<AttendanceResponse> startAttendance(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AttendanceStartRequest request
    ) {
        return ApiResponse.success("출석 기록 완료", attendanceService.startAttendance(contentId, principal.getId(), request));
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{contentId}/attendance")
    public ApiResponse<AttendanceResponse> finishAttendance(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AttendanceEndRequest request
    ) {
        return ApiResponse.success("퇴장 기록 완료", attendanceService.finishAttendance(contentId, principal.getId(), request));
    }
}
