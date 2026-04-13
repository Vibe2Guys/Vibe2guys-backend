package com.vibe2guys.backend.team.controller;

import com.vibe2guys.backend.common.response.ApiResponse;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.team.dto.AutoGroupingRequest;
import com.vibe2guys.backend.team.dto.AutoGroupingResponse;
import com.vibe2guys.backend.team.dto.ChatRoomResponse;
import com.vibe2guys.backend.team.dto.CreateTeamChatMessageRequest;
import com.vibe2guys.backend.team.dto.CreateTeamMeetingNoteRequest;
import com.vibe2guys.backend.team.dto.CreateTeamTaskRequest;
import com.vibe2guys.backend.team.dto.TeamAnalyticsResponse;
import com.vibe2guys.backend.team.dto.TeamListItemResponse;
import com.vibe2guys.backend.team.dto.TeamChatMessageResponse;
import com.vibe2guys.backend.team.dto.TeamMeetingNoteResponse;
import com.vibe2guys.backend.team.dto.TeamMemberContributionResponse;
import com.vibe2guys.backend.team.dto.TeamResponse;
import com.vibe2guys.backend.team.dto.TeamTaskResponse;
import com.vibe2guys.backend.team.dto.UpdateTeamMembersRequest;
import com.vibe2guys.backend.team.dto.UpdateTeamTaskStatusRequest;
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

    @GetMapping("/teams/{teamId}/tasks")
    public ApiResponse<List<TeamTaskResponse>> getTeamTasks(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("팀 업무 목록 조회 성공", teamService.getTeamTasks(teamId, principal.getId()));
    }

    @PostMapping("/teams/{teamId}/tasks")
    public ApiResponse<TeamTaskResponse> createTeamTask(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTeamTaskRequest request
    ) {
        return ApiResponse.success("팀 업무 생성 완료", teamService.createTeamTask(teamId, principal.getId(), request));
    }

    @PatchMapping("/teams/{teamId}/tasks/{taskId}/status")
    public ApiResponse<TeamTaskResponse> updateTaskStatus(
            @PathVariable Long teamId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateTeamTaskStatusRequest request
    ) {
        return ApiResponse.success("팀 업무 상태 변경 완료", teamService.updateTaskStatus(teamId, taskId, principal.getId(), request));
    }

    @GetMapping("/teams/{teamId}/meeting-notes")
    public ApiResponse<List<TeamMeetingNoteResponse>> getMeetingNotes(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("회의 메모 조회 성공", teamService.getMeetingNotes(teamId, principal.getId()));
    }

    @PostMapping("/teams/{teamId}/meeting-notes")
    public ApiResponse<TeamMeetingNoteResponse> createMeetingNote(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTeamMeetingNoteRequest request
    ) {
        return ApiResponse.success("회의 메모 저장 완료", teamService.createMeetingNote(teamId, principal.getId(), request));
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

    @GetMapping("/teams/{teamId}/analytics")
    public ApiResponse<TeamAnalyticsResponse> getTeamAnalytics(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("팀 협업 지표 조회 성공", teamService.getTeamAnalytics(teamId, principal.getId()));
    }

    @GetMapping("/teams/{teamId}/members/contributions")
    public ApiResponse<List<TeamMemberContributionResponse>> getMemberContributions(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success("팀원 기여도 조회 성공", teamService.getMemberContributions(teamId, principal.getId()));
    }
}
