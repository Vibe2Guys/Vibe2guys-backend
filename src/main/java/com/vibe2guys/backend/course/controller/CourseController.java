package com.vibe2guys.backend.course.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.response.PageResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.course.dto.CourseLearningLogItemResponse;
import com.vibe2guys.backend.course.dto.CourseListItemResponse;
import com.vibe2guys.backend.course.dto.CourseStudentItemResponse;
import com.vibe2guys.backend.course.dto.CreateCourseRequest;
import com.vibe2guys.backend.course.dto.CreateCourseResponse;
import com.vibe2guys.backend.course.dto.CreateWeekRequest;
import com.vibe2guys.backend.course.dto.CreateWeekResponse;
import com.vibe2guys.backend.course.dto.CourseDetailResponse;
import com.vibe2guys.backend.course.dto.EnrollByCodeRequest;
import com.vibe2guys.backend.course.dto.EnrollmentResponse;
import com.vibe2guys.backend.course.dto.MyCourseItemResponse;
import com.vibe2guys.backend.course.dto.UpdateCourseRequest;
import com.vibe2guys.backend.course.dto.UpdateCourseStudentMemoRequest;
import com.vibe2guys.backend.course.dto.UpdateCourseResponse;
import com.vibe2guys.backend.course.dto.WeekContentItemResponse;
import com.vibe2guys.backend.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public ApiResponse<PageResponse<CourseListItemResponse>> getCourses(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success("강의 목록 조회 성공", courseService.getCourses(principal.getId(), page, size, keyword));
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

    @PatchMapping("/{courseId}")
    public ApiResponse<UpdateCourseResponse> updateCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateCourseRequest request
    ) {
        return ApiResponse.success("강의 수정 완료", courseService.updateCourse(courseId, principal.getId(), request));
    }

    @PostMapping("/{courseId}/enrollments")
    public ApiResponse<EnrollmentResponse> enroll(
            @PathVariable Long courseId,
            @RequestBody(required = false) Map<String, Object> ignored,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("수강 신청 완료", courseService.enroll(courseId, principal.getId()));
    }

    @PostMapping("/enroll-by-code")
    public ApiResponse<EnrollmentResponse> enrollByCode(
            @Valid @RequestBody EnrollByCodeRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("강의 코드 등록 완료", courseService.enrollByCode(request.courseCode(), principal.getId()));
    }

    @PostMapping("/{courseId}/weeks")
    public ApiResponse<CreateWeekResponse> createWeek(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateWeekRequest request
    ) {
        return ApiResponse.success("주차 생성 완료", courseService.createWeek(courseId, principal.getId(), request));
    }

    @GetMapping("/{courseId}/students")
    public ApiResponse<PageResponse<CourseStudentItemResponse>> getStudents(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success("수강생 목록 조회 성공", courseService.getStudents(courseId, principal.getId(), page, size, keyword));
    }

    @PatchMapping("/{courseId}/students/{studentId}/memo")
    public ApiResponse<CourseStudentItemResponse> updateStudentMemo(
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateCourseStudentMemoRequest request
    ) {
        return ApiResponse.success(
                "수강생 메모 저장 완료",
                courseService.updateStudentMemo(courseId, studentId, principal.getId(), request)
        );
    }

    @GetMapping("/{courseId}/weeks/{weekId}/contents")
    public ApiResponse<List<WeekContentItemResponse>> getWeekContents(
            @PathVariable Long courseId,
            @PathVariable Long weekId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("주차 콘텐츠 목록 조회 성공", courseService.getWeekContents(courseId, weekId, principal.getId()));
    }

    @GetMapping("/{courseId}/learning-logs/me")
    public ApiResponse<List<CourseLearningLogItemResponse>> getMyLearningLogs(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("내 학습 로그 조회 성공", courseService.getMyLearningLogs(courseId, principal.getId()));
    }

}
