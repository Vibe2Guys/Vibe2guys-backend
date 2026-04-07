package com.vibe2guys.backend.course.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.course.dto.ContentDetailResponse;
import com.vibe2guys.backend.course.dto.CreateContentRequest;
import com.vibe2guys.backend.course.dto.CreateContentResponse;
import com.vibe2guys.backend.course.service.CourseService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ContentController {

    private final CourseService courseService;

    @PostMapping("/weeks/{weekId}/contents")
    public ApiResponse<CreateContentResponse> createContent(
            @PathVariable Long weekId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateContentRequest request
    ) {
        return ApiResponse.success("콘텐츠 생성 완료", courseService.createContent(weekId, principal.getId(), request));
    }

    @GetMapping("/contents/{contentId}")
    public ApiResponse<ContentDetailResponse> getContentDetail(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("콘텐츠 상세 조회 성공", courseService.getContentDetail(contentId, principal.getId()));
    }
}
