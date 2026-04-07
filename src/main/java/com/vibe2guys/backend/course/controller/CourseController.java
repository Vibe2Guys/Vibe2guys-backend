package com.vibe2guys.backend.course.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.course.dto.EnrollmentResponse;
import com.vibe2guys.backend.course.dto.MyCourseItemResponse;
import com.vibe2guys.backend.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/my")
    public ApiResponse<List<MyCourseItemResponse>> getMyCourses(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("내 강의 목록 조회 성공", courseService.getMyCourses(principal.getId()));
    }

    @PostMapping("/{courseId}/enrollments")
    public ApiResponse<EnrollmentResponse> enroll(
            @PathVariable Long courseId,
            @RequestBody(required = false) Map<String, Object> ignored,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("수강 신청 완료", courseService.enroll(courseId, principal.getId()));
    }
}
