package com.vibe2guys.backend.admin.controller;

import com.vibe2guys.backend.admin.dto.AdminUserItemResponse;
import com.vibe2guys.backend.admin.dto.AnalyticsConfigResponse;
import com.vibe2guys.backend.admin.dto.CreateAdminUserRequest;
import com.vibe2guys.backend.admin.dto.CreateAdminUserResponse;
import com.vibe2guys.backend.admin.dto.UpdateAnalyticsConfigRequest;
import com.vibe2guys.backend.admin.service.AdminService;
import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.response.PageResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminUserItemResponse>> getUsers(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success("사용자 목록 조회 성공", adminService.getUsers(principal.getId(), page, size, role, keyword));
    }

    @PostMapping("/users")
    public ApiResponse<CreateAdminUserResponse> createUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateAdminUserRequest request
    ) {
        return ApiResponse.success("계정 생성 완료", adminService.createUser(principal.getId(), request));
    }

    @GetMapping("/analytics-config")
    public ApiResponse<AnalyticsConfigResponse> getAnalyticsConfig(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("분석 기준 조회 성공", adminService.getAnalyticsConfig(principal.getId()));
    }

    @PatchMapping("/analytics-config")
    public ApiResponse<AnalyticsConfigResponse> updateAnalyticsConfig(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateAnalyticsConfigRequest request
    ) {
        return ApiResponse.success("분석 기준 수정 완료", adminService.updateAnalyticsConfig(principal.getId(), request));
    }
}
