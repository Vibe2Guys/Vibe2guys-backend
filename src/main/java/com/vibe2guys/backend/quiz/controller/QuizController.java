package com.vibe2guys.backend.quiz.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.quiz.dto.CreateQuizRequest;
import com.vibe2guys.backend.quiz.dto.CreateQuizResponse;
import com.vibe2guys.backend.quiz.dto.CreateQuizSubmissionRequest;
import com.vibe2guys.backend.quiz.dto.QuizDetailResponse;
import com.vibe2guys.backend.quiz.dto.QuizListItemResponse;
import com.vibe2guys.backend.quiz.dto.QuizResultResponse;
import com.vibe2guys.backend.quiz.dto.QuizSubmissionResponse;
import com.vibe2guys.backend.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/api/v1/courses/{courseId}/quizzes")
    public ApiResponse<CreateQuizResponse> createQuiz(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateQuizRequest request
    ) {
        return ApiResponse.success("퀴즈 생성 완료", quizService.createQuiz(courseId, principal.getId(), request));
    }

    @GetMapping("/api/v1/courses/{courseId}/quizzes")
    public ApiResponse<List<QuizListItemResponse>> getQuizzes(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("퀴즈 목록 조회 성공", quizService.getQuizzes(courseId, principal.getId()));
    }

    @GetMapping("/api/v1/quizzes/{quizId}")
    public ApiResponse<QuizDetailResponse> getQuizDetail(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("퀴즈 상세 조회 성공", quizService.getQuizDetail(quizId, principal.getId()));
    }

    @PostMapping("/api/v1/quizzes/{quizId}/submissions")
    public ApiResponse<QuizSubmissionResponse> submitQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateQuizSubmissionRequest request
    ) {
        return ApiResponse.success("퀴즈 제출 완료", quizService.submitQuiz(quizId, principal.getId(), request));
    }

    @GetMapping("/api/v1/quizzes/{quizId}/results/me")
    public ApiResponse<QuizResultResponse> getMyResult(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("퀴즈 결과 조회 성공", quizService.getMyResult(quizId, principal.getId()));
    }
}
