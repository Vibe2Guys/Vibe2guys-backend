package com.vibe2guys.backend.team.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.team.dto.AutoGroupingRequest;
import com.vibe2guys.backend.team.dto.AutoGroupingResponse;
import com.vibe2guys.backend.team.dto.ChatRoomResponse;
import com.vibe2guys.backend.team.dto.CreateTeamChatMessageRequest;
import com.vibe2guys.backend.team.dto.TeamListItemResponse;
import com.vibe2guys.backend.team.dto.TeamChatMessageResponse;
import com.vibe2guys.backend.team.dto.TeamResponse;
import com.vibe2guys.backend.team.dto.UpdateTeamMembersRequest;
import com.vibe2guys.backend.team.service.TeamService;
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
@RequestMapping("/api/v1")
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/courses/{courseId}/teams/auto-grouping")
    public ApiResponse<AutoGroupingResponse> autoGrouping(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody(required = false) AutoGroupingRequest request
    ) {
        return ApiResponse.success("팀 자동 배정 완료", teamService.autoGrouping(courseId, principal.getId(), request));
    }

    @GetMapping("/courses/{courseId}/teams")
    public ApiResponse<List<TeamListItemResponse>> getCourseTeams(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("팀 목록 조회 성공", teamService.getCourseTeams(courseId, principal.getId()));
    }

    @GetMapping("/teams/me")
    public ApiResponse<List<TeamListItemResponse>> getMyTeams(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("내 팀 조회 성공", teamService.getMyTeams(principal.getId()));
    }

    @GetMapping("/teams/{teamId}")
    public ApiResponse<TeamResponse> getTeamDetail(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("팀 상세 조회 성공", teamService.getTeamDetail(teamId, principal.getId()));
    }

    @PatchMapping("/teams/{teamId}/members")
    public ApiResponse<TeamResponse> updateTeamMembers(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) UpdateTeamMembersRequest request
    ) {
        return ApiResponse.success("팀원 재배치 완료", teamService.updateTeamMembers(teamId, principal.getId(), request));
    }

    @GetMapping("/teams/{teamId}/chat-room")
    public ApiResponse<ChatRoomResponse> getChatRoom(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("팀 채팅방 조회 성공", teamService.getChatRoom(teamId, principal.getId()));
    }

    @GetMapping("/chat-rooms/{chatRoomId}/messages")
    public ApiResponse<List<TeamChatMessageResponse>> getMessages(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("팀 채팅 메시지 조회 성공", teamService.getMessages(chatRoomId, principal.getId()));
    }

    @PostMapping("/chat-rooms/{chatRoomId}/messages")
    public ApiResponse<TeamChatMessageResponse> createMessage(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTeamChatMessageRequest request
    ) {
        return ApiResponse.success("팀 채팅 메시지 저장 완료", teamService.createMessage(chatRoomId, principal.getId(), request));
    }
}
