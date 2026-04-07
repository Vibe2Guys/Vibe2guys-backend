package com.vibe2guys.backend.course.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.course.dto.CreateCourseRequest;
import com.vibe2guys.backend.course.dto.CreateCourseResponse;
import com.vibe2guys.backend.course.dto.CreateWeekRequest;
import com.vibe2guys.backend.course.dto.CreateWeekResponse;
import com.vibe2guys.backend.course.dto.CourseDetailResponse;
import com.vibe2guys.backend.course.dto.EnrollmentResponse;
import com.vibe2guys.backend.course.dto.MyCourseItemResponse;
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

    @PostMapping
    public ApiResponse<CreateCourseResponse> createCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCourseRequest request
    ) {
        return ApiResponse.success("강의 생성 완료", courseService.createCourse(principal.getId(), request));
    }

    @GetMapping("/{courseId}")
    public ApiResponse<CourseDetailResponse> getCourseDetail(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("강의 상세 조회 성공", courseService.getCourseDetail(courseId, principal.getId()));
    }

    @PostMapping("/{courseId}/enrollments")
    public ApiResponse<EnrollmentResponse> enroll(
            @PathVariable Long courseId,
            @RequestBody(required = false) Map<String, Object> ignored,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("수강 신청 완료", courseService.enroll(courseId, principal.getId()));
    }

    @PostMapping("/{courseId}/weeks")
    public ApiResponse<CreateWeekResponse> createWeek(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateWeekRequest request
    ) {
        return ApiResponse.success("주차 생성 완료", courseService.createWeek(courseId, principal.getId(), request));
    }

}
