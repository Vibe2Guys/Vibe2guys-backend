package com.vibe2guys.backend.team.service;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.course.domain.CourseEnrollment;
import com.vibe2guys.backend.course.domain.CourseInstructor;
import com.vibe2guys.backend.course.domain.EnrollmentStatus;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.course.repository.CourseInstructorRepository;
import com.vibe2guys.backend.course.repository.CourseRepository;
import com.vibe2guys.backend.team.domain.Team;
import com.vibe2guys.backend.team.domain.TeamChatRoom;
import com.vibe2guys.backend.team.domain.TeamChatMessage;
import com.vibe2guys.backend.team.domain.TeamMember;
import com.vibe2guys.backend.team.domain.TeamMemberStatus;
import com.vibe2guys.backend.team.domain.TeamStatus;
import com.vibe2guys.backend.team.dto.AutoGroupingRequest;
import com.vibe2guys.backend.team.dto.AutoGroupingResponse;
import com.vibe2guys.backend.team.dto.ChatRoomResponse;
import com.vibe2guys.backend.team.dto.CreateTeamChatMessageRequest;
import com.vibe2guys.backend.team.dto.TeamAnalyticsResponse;
import com.vibe2guys.backend.team.dto.TeamListItemResponse;
import com.vibe2guys.backend.team.dto.TeamChatMessageResponse;
import com.vibe2guys.backend.team.dto.TeamMemberContributionResponse;
import com.vibe2guys.backend.team.dto.TeamMemberResponse;
import com.vibe2guys.backend.team.dto.TeamResponse;
import com.vibe2guys.backend.team.dto.UpdateTeamMembersRequest;
import com.vibe2guys.backend.team.repository.TeamChatMessageRepository;
import com.vibe2guys.backend.team.repository.TeamChatRoomRepository;
import com.vibe2guys.backend.team.repository.TeamMemberRepository;
import com.vibe2guys.backend.team.repository.TeamRepository;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamChatRoomRepository teamChatRoomRepository;
    private final TeamChatMessageRepository teamChatMessageRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final UserService userService;

    @Transactional
    public AutoGroupingResponse autoGrouping(Long courseId, Long userId, AutoGroupingRequest request) {
        User user = userService.getById(userId);
        Course course = getManageableCourse(courseId, user);
        int teamSize = request != null && request.teamSize() != null ? request.teamSize() : 4;

        List<CourseEnrollment> enrollments = new ArrayList<>(courseEnrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ENROLLED));
        if (enrollments.size() < 2) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "팀 배정을 하려면 최소 2명의 수강생이 필요합니다.");
        }

        archiveExistingTeams(courseId);
        Collections.shuffle(enrollments);

        List<TeamListItemResponse> createdTeams = new ArrayList<>();
        int teamCount = 0;
        for (int i = 0; i < enrollments.size(); i += teamSize) {
            teamCount++;
            Team team = teamRepository.save(Team.builder()
                    .course(course)
                    .name("Team " + teamCount)
                    .status(TeamStatus.ACTIVE)
                    .build());
            teamChatRoomRepository.save(TeamChatRoom.builder().team(team).build());

            int end = Math.min(i + teamSize, enrollments.size());
            for (int index = i; index < end; index++) {
                CourseEnrollment enrollment = enrollments.get(index);
                teamMemberRepository.save(TeamMember.builder()
                        .team(team)
                        .user(enrollment.getStudent())
                        .joinedAt(OffsetDateTime.now())
                        .status(TeamMemberStatus.ACTIVE)
                        .build());
            }
            createdTeams.add(TeamListItemResponse.of(team, end - i));
        }

        return new AutoGroupingResponse(createdTeams.size(), createdTeams);
    }

    public List<TeamListItemResponse> getCourseTeams(Long courseId, Long userId) {
        User user = userService.getById(userId);
        getAccessibleCourse(courseId, user);
        return teamRepository.findByCourseIdAndStatusOrderByIdAsc(courseId, TeamStatus.ACTIVE).stream()
                .map(team -> TeamListItemResponse.of(team, teamMemberRepository.findByTeamIdAndStatusOrderByIdAsc(team.getId(), TeamMemberStatus.ACTIVE).size()))
                .toList();
    }

    public List<TeamListItemResponse> getMyTeams(Long userId) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "학생만 자신의 팀을 조회할 수 있습니다.");
        }
        return teamMemberRepository.findByUserIdAndStatus(userId, TeamMemberStatus.ACTIVE).stream()
                .map(teamMember -> TeamListItemResponse.of(
                        teamMember.getTeam(),
                        teamMemberRepository.findByTeamIdAndStatusOrderByIdAsc(teamMember.getTeam().getId(), TeamMemberStatus.ACTIVE).size()
                ))
                .toList();
    }

    public TeamResponse getTeamDetail(Long teamId, Long userId) {
        User user = userService.getById(userId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "팀을 찾을 수 없습니다."));
        validateTeamAccess(team, user);

        List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatusOrderByIdAsc(teamId, TeamMemberStatus.ACTIVE);
        List<String> riskSignals = new ArrayList<>();
        if (members.size() < 3) {
            riskSignals.add("팀 인원이 적어 협업 다양성이 낮을 수 있습니다.");
        }

        return new TeamResponse(
                team.getId(),
                team.getCourse().getId(),
                team.getName(),
                team.getStatus().name(),
                50,
                50,
                0,
                0,
                riskSignals,
                members.stream().map(TeamMemberResponse::from).toList()
        );
    }

    @Transactional
    public TeamResponse updateTeamMembers(Long teamId, Long userId, UpdateTeamMembersRequest request) {
        User user = userService.getById(userId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "팀을 찾을 수 없습니다."));
        getManageableCourse(team.getCourse().getId(), user);

        List<Long> removeMemberIds = request != null && request.removeMemberIds() != null ? request.removeMemberIds() : List.of();
        List<Long> addMemberIds = request != null && request.addMemberIds() != null ? request.addMemberIds() : List.of();

        OffsetDateTime now = OffsetDateTime.now();
        for (Long removeMemberId : removeMemberIds) {
            teamMemberRepository.findByTeamIdAndUserIdAndStatus(teamId, removeMemberId, TeamMemberStatus.ACTIVE)
                    .ifPresent(member -> member.remove(now));
        }

        for (Long addMemberId : addMemberIds) {
            ensureEnrollActive(team.getCourse().getId(), addMemberId);

            for (TeamMember existingMember : teamMemberRepository.findByTeamCourseIdAndUserIdAndStatus(team.getCourse().getId(), addMemberId, TeamMemberStatus.ACTIVE)) {
                existingMember.remove(now);
            }

            teamMemberRepository.save(TeamMember.builder()
                    .team(team)
                    .user(userService.getById(addMemberId))
                    .joinedAt(now)
                    .status(TeamMemberStatus.ACTIVE)
                    .build());
        }

        return getTeamDetail(teamId, userId);
    }

    public ChatRoomResponse getChatRoom(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "팀을 찾을 수 없습니다."));
        validateTeamAccess(team, userService.getById(userId));
        TeamChatRoom chatRoom = teamChatRoomRepository.findByTeamId(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "팀 채팅방을 찾을 수 없습니다."));
        return ChatRoomResponse.from(chatRoom);
    }

    public List<TeamChatMessageResponse> getMessages(Long chatRoomId, Long userId) {
        TeamChatRoom chatRoom = teamChatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "팀 채팅방을 찾을 수 없습니다."));
        validateTeamAccess(chatRoom.getTeam(), userService.getById(userId));
        return teamChatMessageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId).stream()
                .map(TeamChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public TeamChatMessageResponse createMessage(Long chatRoomId, Long userId, CreateTeamChatMessageRequest request) {
        User user = userService.getById(userId);
        TeamChatRoom chatRoom = teamChatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "팀 채팅방을 찾을 수 없습니다."));
        Team team = chatRoom.getTeam();
        ensureStudentTeamMember(team.getId(), user);

        TeamChatMessage message = teamChatMessageRepository.save(TeamChatMessage.builder()
                .chatRoom(chatRoom)
                .team(team)
                .sender(user)
                .messageBody(request.messageBody().trim())
                .sentAt(OffsetDateTime.now())
                .build());
        return TeamChatMessageResponse.from(message);
    }

    public TeamAnalyticsResponse getTeamAnalytics(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "팀을 찾을 수 없습니다."));
        validateTeamAccess(team, userService.getById(userId));

        List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatusOrderByIdAsc(teamId, TeamMemberStatus.ACTIVE);
        List<TeamChatMessage> messages = teamChatMessageRepository.findByTeamIdOrderBySentAtAsc(teamId);
        Map<Long, Integer> counts = countMessages(messages);

        int inactiveMemberCount = (int) members.stream().filter(member -> counts.getOrDefault(member.getUser().getId(), 0) == 0).count();
        int totalMessages = counts.values().stream().mapToInt(Integer::intValue).sum();
        int dominantMemberCount = totalMessages == 0 ? 0 : (int) members.stream()
                .filter(member -> counts.getOrDefault(member.getUser().getId(), 0) * 100 / totalMessages >= 60)
                .count();
        int conversationBalanceScore = calculateConversationBalanceScore(members, counts, totalMessages);
        int collaborationScore = clamp(100 - (inactiveMemberCount * 20) - (dominantMemberCount * 15) + Math.min(totalMessages, 15));

        List<String> riskSignals = new ArrayList<>();
        if (inactiveMemberCount > 0) {
            riskSignals.add("대화에 거의 참여하지 않은 팀원이 있습니다.");
        }
        if (dominantMemberCount > 0) {
            riskSignals.add("특정 팀원에게 대화가 과도하게 집중되어 있습니다.");
        }

        return new TeamAnalyticsResponse(
                collaborationScore,
                conversationBalanceScore,
                inactiveMemberCount,
                dominantMemberCount,
                riskSignals
        );
    }

    public List<TeamMemberContributionResponse> getMemberContributions(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "팀을 찾을 수 없습니다."));
        validateTeamAccess(team, userService.getById(userId));

        List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatusOrderByIdAsc(teamId, TeamMemberStatus.ACTIVE);
        Map<Long, Integer> counts = countMessages(teamChatMessageRepository.findByTeamIdOrderBySentAtAsc(teamId));
        int maxCount = counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);

        return members.stream()
                .map(member -> new TeamMemberContributionResponse(
                        member.getUser().getId(),
                        member.getUser().getName(),
                        counts.getOrDefault(member.getUser().getId(), 0),
                        maxCount == 0 ? 0 : clamp(counts.getOrDefault(member.getUser().getId(), 0) * 100 / maxCount)
                ))
                .sorted(Comparator.comparing(TeamMemberContributionResponse::contributionScore).reversed())
                .toList();
    }

    private void archiveExistingTeams(Long courseId) {
        OffsetDateTime now = OffsetDateTime.now();
        for (Team team : teamRepository.findByCourseIdAndStatusOrderByIdAsc(courseId, TeamStatus.ACTIVE)) {
            team.reconfigured();
            for (TeamMember member : teamMemberRepository.findByTeamIdAndStatusOrderByIdAsc(team.getId(), TeamMemberStatus.ACTIVE)) {
                member.remove(now);
            }
        }
    }

    private Course getManageableCourse(Long courseId, User user) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "강의를 찾을 수 없습니다."));
        if (user.getRole() == UserRole.ADMIN) {
            return course;
        }
        if (user.getRole() != UserRole.INSTRUCTOR) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "강의 관리 권한이 없습니다.");
        }
        courseInstructorRepository.findByInstructorId(user.getId()).stream()
                .filter(item -> item.getCourse().getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의만 관리할 수 있습니다."));
        return course;
    }

    private Course getAccessibleCourse(Long courseId, User user) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "강의를 찾을 수 없습니다."));
        if (user.getRole() == UserRole.ADMIN) {
            return course;
        }
        if (user.getRole() == UserRole.INSTRUCTOR) {
            CourseInstructor instructor = courseInstructorRepository.findByInstructorId(user.getId()).stream()
                    .filter(item -> item.getCourse().getId().equals(courseId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의만 접근할 수 있습니다."));
            return instructor.getCourse();
        }
        courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, user.getId())
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 강의만 접근할 수 있습니다."));
        return course;
    }

    private void validateTeamAccess(Team team, User user) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }
        if (user.getRole() == UserRole.INSTRUCTOR) {
            courseInstructorRepository.findByInstructorId(user.getId()).stream()
                    .filter(item -> item.getCourse().getId().equals(team.getCourse().getId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의의 팀만 조회할 수 있습니다."));
            return;
        }
        teamMemberRepository.findByTeamIdAndUserIdAndStatus(team.getId(), user.getId(), TeamMemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "소속된 팀만 조회할 수 있습니다."));
    }

    private void ensureEnrollActive(Long courseId, Long userId) {
        courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, userId)
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 학생만 팀에 배정할 수 있습니다."));
    }

    private void ensureStudentTeamMember(Long teamId, User user) {
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "학생만 팀 채팅 메시지를 보낼 수 있습니다.");
        }
        teamMemberRepository.findByTeamIdAndUserIdAndStatus(teamId, user.getId(), TeamMemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "소속된 팀 채팅방에만 메시지를 보낼 수 있습니다."));
    }

    private Map<Long, Integer> countMessages(List<TeamChatMessage> messages) {
        Map<Long, Integer> counts = new HashMap<>();
        for (TeamChatMessage message : messages) {
            counts.merge(message.getSender().getId(), 1, Integer::sum);
        }
        return counts;
    }

    private int calculateConversationBalanceScore(List<TeamMember> members, Map<Long, Integer> counts, int totalMessages) {
        if (members.isEmpty() || totalMessages == 0) {
            return 0;
        }
        double idealShare = 1.0 / members.size();
        double imbalance = 0.0;
        for (TeamMember member : members) {
            double actualShare = counts.getOrDefault(member.getUser().getId(), 0) / (double) totalMessages;
            imbalance += Math.abs(idealShare - actualShare);
        }
        return clamp((int) Math.round(100 - (imbalance * 100)));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
