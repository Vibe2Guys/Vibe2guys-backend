package com.vibe2guys.backend.assignment.controller;

import com.vibe2guys.backend.assignment.dto.AssignmentDetailResponse;
import com.vibe2guys.backend.assignment.dto.AssignmentListItemResponse;
import com.vibe2guys.backend.assignment.dto.AssignmentSubmissionListItemResponse;
import com.vibe2guys.backend.assignment.dto.AssignmentSubmissionResponse;
import com.vibe2guys.backend.assignment.dto.CreateAssignmentRequest;
import com.vibe2guys.backend.assignment.dto.CreateAssignmentResponse;
import com.vibe2guys.backend.assignment.dto.CreateAssignmentSubmissionRequest;
import com.vibe2guys.backend.assignment.service.AssignmentService;
import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/api/v1/courses/{courseId}/assignments")
    public ApiResponse<CreateAssignmentResponse> createAssignment(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateAssignmentRequest request
    ) {
        return ApiResponse.success("과제 생성 완료", assignmentService.createAssignment(courseId, principal.getId(), request));
    }

    @GetMapping("/api/v1/courses/{courseId}/assignments")
    public ApiResponse<List<AssignmentListItemResponse>> getAssignments(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("과제 목록 조회 성공", assignmentService.getAssignments(courseId, principal.getId()));
    }

    @GetMapping("/api/v1/assignments/{assignmentId}")
    public ApiResponse<AssignmentDetailResponse> getAssignmentDetail(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("과제 상세 조회 성공", assignmentService.getAssignmentDetail(assignmentId, principal.getId()));
    }

    @PostMapping("/api/v1/assignments/{assignmentId}/submissions")
    public ApiResponse<AssignmentSubmissionResponse> submitAssignment(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateAssignmentSubmissionRequest request
    ) {
        return ApiResponse.success("과제 제출 완료", assignmentService.submitAssignment(assignmentId, principal.getId(), request));
    }

    @PatchMapping("/api/v1/assignments/{assignmentId}/submissions/{submissionId}")
    public ApiResponse<AssignmentSubmissionResponse> resubmitAssignment(
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateAssignmentSubmissionRequest request
    ) {
        return ApiResponse.success("과제 재제출 완료", assignmentService.resubmitAssignment(assignmentId, submissionId, principal.getId(), request));
    }

    @GetMapping("/api/v1/assignments/{assignmentId}/submissions")
    public ApiResponse<List<AssignmentSubmissionListItemResponse>> getSubmissions(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("과제 제출 목록 조회 성공", assignmentService.getSubmissions(assignmentId, principal.getId()));
    }
}
