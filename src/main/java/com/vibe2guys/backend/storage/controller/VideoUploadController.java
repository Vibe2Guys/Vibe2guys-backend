package com.vibe2guys.backend.storage.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.storage.dto.CreateVideoUploadUrlRequest;
import com.vibe2guys.backend.storage.dto.VideoUploadUrlResponse;
import com.vibe2guys.backend.storage.service.VideoUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/uploads")
public class VideoUploadController {

    private final VideoUploadService videoUploadService;

    @PostMapping("/videos/presigned-url")
    public ApiResponse<VideoUploadUrlResponse> createVideoUploadUrl(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateVideoUploadUrlRequest request
    ) {
        return ApiResponse.success("비디오 업로드 URL 생성 완료", videoUploadService.createVideoUploadUrl(principal.getId(), request));
    }
}
