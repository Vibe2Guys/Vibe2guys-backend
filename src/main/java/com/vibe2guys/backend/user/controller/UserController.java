package com.vibe2guys.backend.user.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.user.dto.MyProfileResponse;
import com.vibe2guys.backend.user.dto.UpdateMyProfileRequest;
import com.vibe2guys.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<MyProfileResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("내 정보 조회 성공", userService.getMyProfile(principal.getId()));
    }

    @PatchMapping("/me")
    public ApiResponse<MyProfileResponse> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        return ApiResponse.success("내 정보 수정 완료", userService.updateMyProfile(principal.getId(), request));
    }
}
