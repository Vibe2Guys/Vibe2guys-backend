package com.vibe2guys.backend.analytics.controller;

import com.vibe2guys.backend.analytics.dto.InstructorDashboardResponse;
import com.vibe2guys.backend.analytics.dto.InstructorRiskStudentItemResponse;
import com.vibe2guys.backend.analytics.dto.MyReportResponse;
import com.vibe2guys.backend.analytics.dto.StudentRecommendationsResponse;
import com.vibe2guys.backend.analytics.dto.StudentRiskResponse;
import com.vibe2guys.backend.analytics.dto.StudentDashboardResponse;
import com.vibe2guys.backend.analytics.dto.StudentScoresResponse;
import com.vibe2guys.backend.analytics.service.AnalyticsService;
import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/students/{studentId}/scores")
    public ApiResponse<StudentScoresResponse> getStudentScores(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("학생 점수 조회 성공", analyticsService.getStudentScores(studentId, principal.getId()));
    }

    @GetMapping("/students/{studentId}/risk")
    public ApiResponse<StudentRiskResponse> getStudentRisk(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("학생 위험도 조회 성공", analyticsService.getStudentRisk(studentId, principal.getId()));
    }

    @GetMapping("/students/{studentId}/recommendations")
    public ApiResponse<StudentRecommendationsResponse> getRecommendations(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("맞춤 추천 조회 성공", analyticsService.getRecommendations(studentId, principal.getId()));
    }

    @GetMapping("/dashboard/student")
    public ApiResponse<StudentDashboardResponse> getStudentDashboard(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("학습자 대시보드 조회 성공", analyticsService.getStudentDashboard(principal.getId()));
    }

    @GetMapping("/reports/me")
    public ApiResponse<MyReportResponse> getMyReport(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("내 리포트 조회 성공", analyticsService.getMyReport(principal.getId()));
    }

    @GetMapping("/dashboard/instructor/courses/{courseId}")
    public ApiResponse<InstructorDashboardResponse> getInstructorDashboard(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("교수자 대시보드 조회 성공", analyticsService.getInstructorDashboard(courseId, principal.getId()));
    }

    @GetMapping("/instructors/courses/{courseId}/students/risk")
    public ApiResponse<List<InstructorRiskStudentItemResponse>> getRiskStudents(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("위험 학생 목록 조회 성공", analyticsService.getRiskStudents(courseId, principal.getId()));
    }
}
