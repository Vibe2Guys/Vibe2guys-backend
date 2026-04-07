package com.vibe2guys.backend.ai.controller;

import com.vibe2guys.backend.ai.dto.CreateFollowUpQuestionRequest;
import com.vibe2guys.backend.ai.dto.CreateFollowUpResponseRequest;
import com.vibe2guys.backend.ai.dto.FollowUpAnalysisResponse;
import com.vibe2guys.backend.ai.dto.FollowUpAnswerResponse;
import com.vibe2guys.backend.ai.dto.FollowUpQuestionResponse;
import com.vibe2guys.backend.ai.service.AiService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;

    @PostMapping("/follow-up-questions")
    public ApiResponse<FollowUpQuestionResponse> createFollowUpQuestion(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateFollowUpQuestionRequest request
    ) {
        return ApiResponse.success("꼬리질문 생성 완료", aiService.createFollowUpQuestion(principal.getId(), request));
    }

    @PostMapping("/follow-up-questions/{questionId}/responses")
    public ApiResponse<FollowUpAnswerResponse> createFollowUpResponse(
            @PathVariable Long questionId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateFollowUpResponseRequest request
    ) {
        return ApiResponse.success("꼬리질문 답변 저장 완료", aiService.createFollowUpResponse(questionId, principal.getId(), request));
    }

    @GetMapping("/follow-up-questions/{questionId}/analysis")
    public ApiResponse<FollowUpAnalysisResponse> getFollowUpAnalysis(
            @PathVariable Long questionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("꼬리질문 분석 조회 성공", aiService.getFollowUpAnalysis(questionId, principal.getId()));
    }
}
