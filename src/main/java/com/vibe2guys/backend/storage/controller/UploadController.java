package com.vibe2guys.backend.storage.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.storage.dto.CreateFileUploadUrlRequest;
import com.vibe2guys.backend.storage.dto.CreateVideoUploadUrlRequest;
import com.vibe2guys.backend.storage.dto.FileUploadCategory;
import com.vibe2guys.backend.storage.dto.FileUploadUrlResponse;
import com.vibe2guys.backend.storage.service.FileUploadService;
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
public class UploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/presigned-url")
    public ApiResponse<FileUploadUrlResponse> createUploadUrl(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateFileUploadUrlRequest request
    ) {
        return ApiResponse.success("파일 업로드 URL 생성 완료", fileUploadService.createUploadUrl(principal.getId(), request));
    }

    @PostMapping("/videos/presigned-url")
    public ApiResponse<FileUploadUrlResponse> createVideoUploadUrl(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateVideoUploadUrlRequest request
    ) {
        CreateFileUploadUrlRequest uploadRequest = new CreateFileUploadUrlRequest(
                request.fileName(),
                request.contentType(),
                FileUploadCategory.CONTENT_VIDEO
        );
        return ApiResponse.success("비디오 업로드 URL 생성 완료", fileUploadService.createUploadUrl(principal.getId(), uploadRequest));
    }
}
