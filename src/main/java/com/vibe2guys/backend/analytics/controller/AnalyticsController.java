package com.vibe2guys.backend.analytics.controller;

import com.vibe2guys.backend.analytics.dto.InstructorDashboardResponse;
import com.vibe2guys.backend.analytics.dto.InstructorInterventionItemResponse;
import com.vibe2guys.backend.analytics.dto.InstructorRiskStudentItemResponse;
import com.vibe2guys.backend.analytics.dto.InstructorStudentDetailResponse;
import com.vibe2guys.backend.analytics.dto.InstructorUnderstandingLowStudentResponse;
import com.vibe2guys.backend.analytics.dto.MyReportResponse;
import com.vibe2guys.backend.analytics.dto.CreateInstructorInterventionRequest;
import com.vibe2guys.backend.analytics.dto.ScoreDistributionResponse;
import com.vibe2guys.backend.analytics.dto.StudentAiUnderstandingResponse;
import com.vibe2guys.backend.analytics.dto.StudentRecommendationsResponse;
import com.vibe2guys.backend.analytics.dto.StudentRiskResponse;
import com.vibe2guys.backend.analytics.dto.StudentDashboardResponse;
import com.vibe2guys.backend.analytics.dto.StudentScoresResponse;
import com.vibe2guys.backend.analytics.service.AnalyticsService;
import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
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

    @GetMapping("/students/{studentId}/ai-understanding")
    public ApiResponse<StudentAiUnderstandingResponse> getStudentAiUnderstanding(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("학생 AI 이해도 조회 성공", analyticsService.getStudentAiUnderstanding(studentId, principal.getId()));
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

    @GetMapping("/instructors/courses/{courseId}/students/understanding-low")
    public ApiResponse<List<InstructorUnderstandingLowStudentResponse>> getLowUnderstandingStudents(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("이해도 낮은 학생 목록 조회 성공", analyticsService.getLowUnderstandingStudents(courseId, principal.getId()));
    }

    @GetMapping("/instructors/courses/{courseId}/students/{studentId}")
    public ApiResponse<InstructorStudentDetailResponse> getInstructorStudentDetail(
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("학생 상세 분석 조회 성공", analyticsService.getInstructorStudentDetail(courseId, studentId, principal.getId()));
    }

    @GetMapping("/instructors/courses/{courseId}/interventions")
    public ApiResponse<List<InstructorInterventionItemResponse>> getInterventions(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("개입 목록 조회 성공", analyticsService.getInterventions(courseId, principal.getId()));
    }

    @PostMapping("/instructors/courses/{courseId}/interventions")
    public ApiResponse<InstructorInterventionItemResponse> createIntervention(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateInstructorInterventionRequest request
    ) {
        return ApiResponse.success("개입 등록 완료", analyticsService.createIntervention(courseId, principal.getId(), request));
    }

    @GetMapping("/instructors/courses/{courseId}/score-distribution")
    public ApiResponse<ScoreDistributionResponse> getScoreDistribution(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("점수 분포 조회 성공", analyticsService.getScoreDistribution(courseId, principal.getId()));
    }
}
