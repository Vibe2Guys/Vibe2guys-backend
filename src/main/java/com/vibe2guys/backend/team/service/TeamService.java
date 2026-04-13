package com.vibe2guys.backend.team.service;

import com.vibe2guys.backend.ai.domain.AiFollowUpAnalysis;
import com.vibe2guys.backend.ai.repository.AiFollowUpAnalysisRepository;
import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.course.domain.CourseEnrollment;
import com.vibe2guys.backend.course.domain.EnrollmentStatus;
import com.vibe2guys.backend.course.repository.ContentRepository;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.course.repository.CourseInstructorRepository;
import com.vibe2guys.backend.course.repository.CourseRepository;
import com.vibe2guys.backend.learning.domain.AttendanceSummary;
import com.vibe2guys.backend.learning.domain.ContentProgressSummary;
import com.vibe2guys.backend.learning.repository.AttendanceSummaryRepository;
import com.vibe2guys.backend.learning.repository.ContentProgressSummaryRepository;
import com.vibe2guys.backend.team.domain.Team;
import com.vibe2guys.backend.team.domain.TeamChatMessage;
import com.vibe2guys.backend.team.domain.TeamChatRoom;
import com.vibe2guys.backend.team.domain.TeamLearningStyle;
import com.vibe2guys.backend.team.domain.TeamMeetingNote;
import com.vibe2guys.backend.team.domain.TeamMember;
import com.vibe2guys.backend.team.domain.TeamMemberStatus;
import com.vibe2guys.backend.team.domain.TeamStatus;
import com.vibe2guys.backend.team.domain.TeamTask;
import com.vibe2guys.backend.team.domain.TeamTaskStatus;
import com.vibe2guys.backend.team.dto.AutoGroupingRequest;
import com.vibe2guys.backend.team.dto.AutoGroupingResponse;
import com.vibe2guys.backend.team.dto.ChatRoomResponse;
import com.vibe2guys.backend.team.dto.CreateTeamChatMessageRequest;
import com.vibe2guys.backend.team.dto.CreateTeamMeetingNoteRequest;
import com.vibe2guys.backend.team.dto.CreateTeamTaskRequest;
import com.vibe2guys.backend.team.dto.TeamAnalyticsResponse;
import com.vibe2guys.backend.team.dto.TeamChatMessageResponse;
import com.vibe2guys.backend.team.dto.TeamListItemResponse;
import com.vibe2guys.backend.team.dto.TeamMeetingNoteResponse;
import com.vibe2guys.backend.team.dto.TeamMemberContributionResponse;
import com.vibe2guys.backend.team.dto.TeamMemberResponse;
import com.vibe2guys.backend.team.dto.TeamResponse;
import com.vibe2guys.backend.team.dto.TeamTaskResponse;
import com.vibe2guys.backend.team.dto.TeamStyleDistributionResponse;
import com.vibe2guys.backend.team.dto.UpdateTeamMembersRequest;
import com.vibe2guys.backend.team.dto.UpdateTeamTaskStatusRequest;
import com.vibe2guys.backend.team.repository.TeamChatMessageRepository;
import com.vibe2guys.backend.team.repository.TeamChatRoomRepository;
import com.vibe2guys.backend.team.repository.TeamMeetingNoteRepository;
import com.vibe2guys.backend.team.repository.TeamMemberCountView;
import com.vibe2guys.backend.team.repository.TeamMemberRepository;
import com.vibe2guys.backend.team.repository.TeamRepository;
import com.vibe2guys.backend.team.repository.TeamTaskRepository;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private static final String GROUPING_BASIS = "LEARNING_STYLE_BALANCED";

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamChatRoomRepository teamChatRoomRepository;
    private final TeamChatMessageRepository teamChatMessageRepository;
    private final TeamTaskRepository teamTaskRepository;
    private final TeamMeetingNoteRepository teamMeetingNoteRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final ContentRepository contentRepository;
    private final ContentProgressSummaryRepository contentProgressSummaryRepository;
    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final AiFollowUpAnalysisRepository aiFollowUpAnalysisRepository;
    private final UserService userService;

    @Transactional
    public AutoGroupingResponse autoGrouping(Long courseId, Long userId, AutoGroupingRequest request) {
        User user = userService.getById(userId);
        Course course = getManageableCourse(courseId, user);
        int teamSize = request != null && request.teamSize() != null ? request.teamSize() : 4;

        List<CourseEnrollment> enrollments = new ArrayList<>(
                courseEnrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ENROLLED)
        );
        if (enrollments.size() < 2) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "팀 배정을 하려면 최소 2명의 수강생이 필요합니다.");
        }

        int contentCount = Math.max(contentRepository.findByCourseIdOrderByIdAsc(courseId).size(), 1);
        List<LearnerProfile> profiles = enrollments.stream()
                .map(enrollment -> buildLearnerProfile(courseId, contentCount, enrollment.getStudent()))
                .sorted(Comparator.comparingInt(LearnerProfile::overallScore).reversed())
                .toList();

        List<TeamDraft> drafts = initializeTeamDrafts(profiles.size(), teamSize);
        for (LearnerProfile profile : profiles) {
            selectBestDraft(drafts, profile, teamSize).profiles().add(profile);
        }

        archiveExistingTeams(courseId);

        List<TeamListItemResponse> createdTeams = new ArrayList<>();
        for (int index = 0; index < drafts.size(); index++) {
            TeamDraft draft = drafts.get(index);
            if (draft.profiles().isEmpty()) {
                continue;
            }
            TeamMatching matching = buildTeamMatching(draft.profiles());
            Team team = teamRepository.save(Team.builder()
                    .course(course)
                    .name(course.getTitle() + " : " + (index + 1) + "팀")
                    .status(TeamStatus.ACTIVE)
                    .teamBuildingScore(matching.teamBuildingScore())
                    .profileDiversityScore(matching.profileDiversityScore())
                    .matchingSummary(matching.matchingSummary())
                    .build());
            teamChatRoomRepository.save(TeamChatRoom.builder().team(team).build());

            OffsetDateTime now = OffsetDateTime.now();
            for (LearnerProfile profile : draft.profiles()) {
                teamMemberRepository.save(TeamMember.builder()
                        .team(team)
                        .user(profile.user())
                        .joinedAt(now)
                        .status(TeamMemberStatus.ACTIVE)
                        .learningStyle(profile.learningStyle())
                        .reliabilityScore(profile.reliabilityScore())
                        .initiativeScore(profile.initiativeScore())
                        .supportScore(profile.supportScore())
                        .understandingScore(profile.understandingScore())
                        .profileSummary(profile.profileSummary())
                        .build());
            }
            createdTeams.add(TeamListItemResponse.of(team, draft.profiles().size()));
        }

        return new AutoGroupingResponse(createdTeams.size(), GROUPING_BASIS, createdTeams);
    }

    public List<TeamListItemResponse> getCourseTeams(Long courseId, Long userId) {
        User user = userService.getById(userId);
        getAccessibleCourse(courseId, user);
        List<Team> teams = teamRepository.findByCourseIdAndStatusOrderByIdAsc(courseId, TeamStatus.ACTIVE);
        Map<Long, Long> memberCounts = loadMemberCounts(teams);
        return teams.stream()
                .map(team -> TeamListItemResponse.of(team, memberCounts.getOrDefault(team.getId(), 0L).intValue()))
                .toList();
    }

    public List<TeamListItemResponse> getMyTeams(Long userId) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "학생만 자신의 팀을 조회할 수 있습니다.");
        }
        List<TeamMember> memberships = teamMemberRepository.findByUserIdAndStatus(userId, TeamMemberStatus.ACTIVE);
        List<Team> teams = memberships.stream().map(TeamMember::getTeam).toList();
        Map<Long, Long> memberCounts = loadMemberCounts(teams);
        return memberships.stream()
                .map(teamMember -> TeamListItemResponse.of(
                        teamMember.getTeam(),
                        memberCounts.getOrDefault(teamMember.getTeam().getId(), 0L).intValue()
                ))
                .toList();
    }

    public TeamResponse getTeamDetail(Long teamId, Long userId) {
        User user = userService.getById(userId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "팀을 찾을 수 없습니다."));
        validateTeamAccess(team, user);

        List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatusOrderByIdAsc(teamId, TeamMemberStatus.ACTIVE);
        CollaborationSnapshot collaboration = buildCollaborationSnapshot(team, members);

        return new TeamResponse(
                team.getId(),
                team.getCourse().getId(),
                team.getName(),
                team.getStatus().name(),
                team.getTeamBuildingScore(),
                team.getProfileDiversityScore(),
                team.getMatchingSummary(),
                collaboration.collaborationScore(),
                collaboration.conversationBalanceScore(),
                collaboration.inactiveMemberCount(),
                collaboration.dominantMemberCount(),
                collaboration.riskSignals(),
                members.stream().map(TeamMemberResponse::from).toList()
        );
    }

    public List<TeamTaskResponse> getTeamTasks(Long teamId, Long userId) {
        Team team = getAccessibleTeam(teamId, userId);
        return teamTaskRepository.findByTeamIdOrderByDueAtAscIdAsc(team.getId()).stream()
                .map(TeamTaskResponse::from)
                .toList();
    }

    @Transactional
    public TeamTaskResponse createTeamTask(Long teamId, Long userId, CreateTeamTaskRequest request) {
        User user = userService.getById(userId);
        Team team = getAccessibleTeam(teamId, userId);
        User assignee = request.assigneeUserId() == null ? null : userService.getById(request.assigneeUserId());
        if (assignee != null) {
            ensureStudentTeamMember(team.getId(), assignee);
        }
        TeamTask task = teamTaskRepository.save(TeamTask.builder()
                .team(team)
                .title(request.title().trim())
                .description(request.description() == null ? null : request.description().trim())
                .assignee(assignee)
                .status(TeamTaskStatus.TODO)
                .dueAt(request.dueAt())
                .createdBy(user)
                .build());
        return TeamTaskResponse.from(task);
    }

    @Transactional
    public TeamTaskResponse updateTaskStatus(Long teamId, Long taskId, Long userId, UpdateTeamTaskStatusRequest request) {
        Team team = getAccessibleTeam(teamId, userId);
        TeamTask task = teamTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "팀 업무를 찾을 수 없습니다."));
        if (!task.getTeam().getId().equals(team.getId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "해당 팀의 업무만 변경할 수 있습니다.");
        }
        task.updateStatus(request.status());
        return TeamTaskResponse.from(task);
    }

    public List<TeamMeetingNoteResponse> getMeetingNotes(Long teamId, Long userId) {
        Team team = getAccessibleTeam(teamId, userId);
        return teamMeetingNoteRepository.findByTeamIdOrderByCreatedAtDesc(team.getId()).stream()
                .map(TeamMeetingNoteResponse::from)
                .toList();
    }

    @Transactional
    public TeamMeetingNoteResponse createMeetingNote(Long teamId, Long userId, CreateTeamMeetingNoteRequest request) {
        User user = userService.getById(userId);
        Team team = getAccessibleTeam(teamId, userId);
        TeamMeetingNote note = teamMeetingNoteRepository.save(TeamMeetingNote.builder()
                .team(team)
                .title(request.title().trim())
                .noteBody(request.noteBody().trim())
                .createdBy(user)
                .build());
        return TeamMeetingNoteResponse.from(note);
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
        int contentCount = Math.max(contentRepository.findByCourseIdOrderByIdAsc(team.getCourse().getId()).size(), 1);
        for (Long removeMemberId : removeMemberIds) {
            teamMemberRepository.findByTeamIdAndUserIdAndStatus(teamId, removeMemberId, TeamMemberStatus.ACTIVE)
                    .ifPresent(member -> member.remove(now));
        }

        for (Long addMemberId : addMemberIds) {
            ensureEnrollActive(team.getCourse().getId(), addMemberId);

            for (TeamMember existingMember : teamMemberRepository.findByTeamCourseIdAndUserIdAndStatus(
                    team.getCourse().getId(),
                    addMemberId,
                    TeamMemberStatus.ACTIVE
            )) {
                existingMember.remove(now);
            }

            User addedUser = userService.getById(addMemberId);
            LearnerProfile profile = buildLearnerProfile(team.getCourse().getId(), contentCount, addedUser);
            teamMemberRepository.save(TeamMember.builder()
                    .team(team)
                    .user(addedUser)
                    .joinedAt(now)
                    .status(TeamMemberStatus.ACTIVE)
                    .learningStyle(profile.learningStyle())
                    .reliabilityScore(profile.reliabilityScore())
                    .initiativeScore(profile.initiativeScore())
                    .supportScore(profile.supportScore())
                    .understandingScore(profile.understandingScore())
                    .profileSummary(profile.profileSummary())
                    .build());
        }

        refreshCourseTeamMatching(team.getCourse().getId());
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
        CollaborationSnapshot collaboration = buildCollaborationSnapshot(team, members);

        return new TeamAnalyticsResponse(
                team.getTeamBuildingScore(),
                team.getProfileDiversityScore(),
                team.getMatchingSummary(),
                collaboration.collaborationScore(),
                collaboration.conversationBalanceScore(),
                collaboration.inactiveMemberCount(),
                collaboration.dominantMemberCount(),
                collaboration.riskSignals(),
                buildStrengthSignals(team, members, collaboration),
                buildStyleDistributions(members)
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
                .map(member -> {
                    int normalizedMessageScore = maxCount == 0 ? 0 : clamp(counts.getOrDefault(member.getUser().getId(), 0) * 100 / maxCount);
                    int contributionScore = clamp((normalizedMessageScore * 70 + member.getReliabilityScore() * 30) / 100);
                    return new TeamMemberContributionResponse(
                            member.getUser().getId(),
                            member.getUser().getName(),
                            member.getLearningStyle().name(),
                            member.getReliabilityScore(),
                            counts.getOrDefault(member.getUser().getId(), 0),
                            contributionScore
                    );
                })
                .sorted(Comparator.comparing(TeamMemberContributionResponse::contributionScore).reversed())
                .toList();
    }

    @Transactional
    protected void refreshCourseTeamMatching(Long courseId) {
        for (Team team : teamRepository.findByCourseIdAndStatusOrderByIdAsc(courseId, TeamStatus.ACTIVE)) {
            List<TeamMember> members = teamMemberRepository.findByTeamIdAndStatusOrderByIdAsc(team.getId(), TeamMemberStatus.ACTIVE);
            TeamMatching matching = buildTeamMatching(members.stream()
                    .map(member -> new LearnerProfile(
                            member.getUser(),
                            member.getLearningStyle(),
                            member.getReliabilityScore(),
                            member.getInitiativeScore(),
                            member.getSupportScore(),
                            member.getUnderstandingScore(),
                            member.getProfileSummary()
                    ))
                    .toList());
            team.updateMatching(matching.teamBuildingScore(), matching.profileDiversityScore(), matching.matchingSummary());
        }
    }

    private LearnerProfile buildLearnerProfile(Long courseId, int contentCount, User student) {
        List<ContentProgressSummary> progressSummaries = contentProgressSummaryRepository.findByCourseIdAndStudentId(courseId, student.getId());
        List<AttendanceSummary> attendanceSummaries = attendanceSummaryRepository.findByCourseIdAndStudentId(courseId, student.getId());
        List<AiFollowUpAnalysis> analyses = aiFollowUpAnalysisRepository
                .findByQuestionStudentIdAndQuestionCourseIdOrderByAnalyzedAtDesc(student.getId(), courseId);

        int averageProgress = average(progressSummaries.stream().map(ContentProgressSummary::getProgressRate).toList());
        int completionRate = progressSummaries.isEmpty()
                ? 0
                : clamp(progressSummaries.stream().mapToInt(summary -> summary.isCompleted() ? 100 : 0).sum() / Math.max(contentCount, 1));
        int attendanceCoverage = clamp(attendanceSummaries.size() * 100 / Math.max(contentCount, 1));
        int followUpParticipation = clamp(Math.min(100, analyses.size() * 25));
        int replaySignal = clamp((int) Math.round(progressSummaries.stream()
                .mapToInt(ContentProgressSummary::getReplayCount)
                .average()
                .orElse(0.0) * 20));
        int understandingScore = analyses.isEmpty()
                ? clamp((averageProgress * 60 + completionRate * 40) / 100)
                : average(analyses.stream().map(AiFollowUpAnalysis::getUnderstandingScore).toList());
        int consistencyScore = clamp(100 - Math.abs(averageProgress - attendanceCoverage));

        int reliabilityScore = clamp((averageProgress * 40 + completionRate * 25 + attendanceCoverage * 35) / 100);
        int initiativeScore = clamp((followUpParticipation * 40 + replaySignal * 20 + averageProgress * 20 + attendanceCoverage * 20) / 100);
        int supportScore = clamp((consistencyScore * 35 + attendanceCoverage * 25 + understandingScore * 20 + completionRate * 20) / 100);
        TeamLearningStyle learningStyle = resolveLearningStyle(reliabilityScore, initiativeScore, supportScore, understandingScore);
        String profileSummary = buildProfileSummary(learningStyle, reliabilityScore, initiativeScore, supportScore, understandingScore);

        return new LearnerProfile(
                student,
                learningStyle,
                reliabilityScore,
                initiativeScore,
                supportScore,
                understandingScore,
                profileSummary
        );
    }

    private Team getAccessibleTeam(Long teamId, Long userId) {
        User user = userService.getById(userId);
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "팀을 찾을 수 없습니다."));
        validateTeamAccess(team, user);
        return team;
    }

    private TeamLearningStyle resolveLearningStyle(int reliabilityScore, int initiativeScore, int supportScore, int understandingScore) {
        if (initiativeScore >= 72 && understandingScore >= 55) {
            return TeamLearningStyle.DRIVER;
        }
        if (understandingScore >= 75) {
            return TeamLearningStyle.ANALYST;
        }
        if (supportScore >= 72) {
            return TeamLearningStyle.FACILITATOR;
        }
        if (reliabilityScore >= 72) {
            return TeamLearningStyle.STEADY;
        }
        return TeamLearningStyle.BALANCED;
    }

    private String buildProfileSummary(
            TeamLearningStyle learningStyle,
            int reliabilityScore,
            int initiativeScore,
            int supportScore,
            int understandingScore
    ) {
        return switch (learningStyle) {
            case DRIVER -> "실행 속도가 빠르고 먼저 논의를 이끄는 편입니다. 추진 " + initiativeScore + ", 이해도 " + understandingScore + ".";
            case ANALYST -> "개념 연결과 문제 구조화가 강점입니다. 이해도 " + understandingScore + ", 신뢰도 " + reliabilityScore + ".";
            case FACILITATOR -> "출석과 협업 안정성이 높아 팀 조율에 강합니다. 지원도 " + supportScore + ", 신뢰도 " + reliabilityScore + ".";
            case STEADY -> "과제와 진도 관리가 안정적입니다. 신뢰도 " + reliabilityScore + ", 지원도 " + supportScore + ".";
            case BALANCED -> "여러 지표가 고르게 분포된 균형형 학습자입니다. 추진 " + initiativeScore + ", 지원 " + supportScore + ".";
        };
    }

    private List<TeamDraft> initializeTeamDrafts(int learnerCount, int teamSize) {
        int teamCount = (int) Math.ceil(learnerCount / (double) teamSize);
        List<TeamDraft> drafts = new ArrayList<>();
        for (int index = 0; index < teamCount; index++) {
            drafts.add(new TeamDraft(new ArrayList<>()));
        }
        return drafts;
    }

    private TeamDraft selectBestDraft(List<TeamDraft> drafts, LearnerProfile profile, int teamSize) {
        TeamDraft bestDraft = null;
        int bestCost = Integer.MAX_VALUE;
        for (TeamDraft draft : drafts) {
            if (draft.profiles().size() >= teamSize) {
                continue;
            }
            int cost = calculatePlacementCost(draft.profiles(), profile);
            if (bestDraft == null || cost < bestCost) {
                bestDraft = draft;
                bestCost = cost;
            }
        }
        return bestDraft == null ? drafts.getFirst() : bestDraft;
    }

    private int calculatePlacementCost(List<LearnerProfile> currentProfiles, LearnerProfile candidate) {
        if (currentProfiles.isEmpty()) {
            return 0;
        }
        List<LearnerProfile> projected = new ArrayList<>(currentProfiles);
        projected.add(candidate);

        int duplicateStylePenalty = (int) currentProfiles.stream()
                .filter(profile -> profile.learningStyle() == candidate.learningStyle())
                .count() * 22;
        int diversityPenalty = 100 - calculateProfileDiversityScore(projected);
        int balancePenalty = 100 - calculateScoreBalance(projected);
        int complementBonus = currentProfiles.stream().noneMatch(profile -> profile.learningStyle() == candidate.learningStyle()) ? 18 : 0;
        return duplicateStylePenalty + diversityPenalty + balancePenalty - complementBonus + currentProfiles.size() * 4;
    }

    private TeamMatching buildTeamMatching(List<LearnerProfile> profiles) {
        if (profiles.isEmpty()) {
            return new TeamMatching(0, 0, "팀원 데이터가 아직 없습니다.");
        }
        int diversityScore = calculateProfileDiversityScore(profiles);
        int scoreBalance = calculateScoreBalance(profiles);
        int teamBuildingScore = clamp((diversityScore * 45 + scoreBalance * 55) / 100);
        return new TeamMatching(teamBuildingScore, diversityScore, buildMatchingSummary(profiles, diversityScore, scoreBalance));
    }

    private int calculateProfileDiversityScore(List<LearnerProfile> profiles) {
        Set<TeamLearningStyle> styles = new HashSet<>();
        for (LearnerProfile profile : profiles) {
            styles.add(profile.learningStyle());
        }
        int normalizedStyleCount = clamp(styles.size() * 100 / Math.min(Math.max(profiles.size(), 1), 4));
        int balancedMetricBonus = calculateScoreBalance(profiles);
        return clamp((normalizedStyleCount * 70 + balancedMetricBonus * 30) / 100);
    }

    private int calculateScoreBalance(List<LearnerProfile> profiles) {
        if (profiles.isEmpty()) {
            return 0;
        }
        int reliabilitySpread = spread(profiles.stream().map(LearnerProfile::reliabilityScore).toList());
        int initiativeSpread = spread(profiles.stream().map(LearnerProfile::initiativeScore).toList());
        int supportSpread = spread(profiles.stream().map(LearnerProfile::supportScore).toList());
        int understandingSpread = spread(profiles.stream().map(LearnerProfile::understandingScore).toList());
        int averageSpread = (reliabilitySpread + initiativeSpread + supportSpread + understandingSpread) / 4;
        return clamp(100 - averageSpread);
    }

    private String buildMatchingSummary(List<LearnerProfile> profiles, int diversityScore, int scoreBalance) {
        Set<TeamLearningStyle> styles = new HashSet<>();
        for (LearnerProfile profile : profiles) {
            styles.add(profile.learningStyle());
        }
        if (styles.contains(TeamLearningStyle.DRIVER)
                && styles.contains(TeamLearningStyle.ANALYST)
                && styles.contains(TeamLearningStyle.FACILITATOR)) {
            return "추진형, 분석형, 조율형을 함께 배치해 실행력과 의사결정 균형을 노렸습니다.";
        }
        if (diversityScore >= 80) {
            return "서로 다른 학습 스타일을 섞어 역할 분담이 자연스럽게 일어나도록 편성했습니다.";
        }
        if (scoreBalance >= 70) {
            return "학습 진도와 이해도 편차를 줄여 팀 내 부담이 한쪽에 쏠리지 않도록 맞췄습니다.";
        }
        return "현재 학습 데이터 기준으로 가장 안정적인 조합이 되도록 균형 위주로 편성했습니다.";
    }

    private CollaborationSnapshot buildCollaborationSnapshot(Team team, List<TeamMember> members) {
        Map<Long, Integer> counts = countMessages(teamChatMessageRepository.findByTeamIdOrderBySentAtAsc(team.getId()));
        int inactiveMemberCount = (int) members.stream().filter(member -> counts.getOrDefault(member.getUser().getId(), 0) == 0).count();
        int totalMessages = counts.values().stream().mapToInt(Integer::intValue).sum();
        int dominantMemberCount = totalMessages == 0 ? 0 : (int) members.stream()
                .filter(member -> counts.getOrDefault(member.getUser().getId(), 0) * 100 / totalMessages >= 60)
                .count();
        int conversationBalanceScore = totalMessages == 0
                ? team.getProfileDiversityScore()
                : calculateConversationBalanceScore(members, counts, totalMessages);
        int liveCollaborationScore = clamp(100 - (inactiveMemberCount * 20) - (dominantMemberCount * 15) + Math.min(totalMessages, 15));
        int collaborationScore = totalMessages == 0
                ? team.getTeamBuildingScore()
                : clamp((team.getTeamBuildingScore() * 35 + liveCollaborationScore * 65) / 100);

        List<String> riskSignals = new ArrayList<>();
        if (team.getProfileDiversityScore() < 55) {
            riskSignals.add("학습 스타일 구성이 유사해 역할 분담이 단조로울 수 있습니다.");
        }
        if (inactiveMemberCount > 0) {
            riskSignals.add("대화에 거의 참여하지 않은 팀원이 있습니다.");
        }
        if (dominantMemberCount > 0) {
            riskSignals.add("특정 팀원에게 대화가 과도하게 집중되어 있습니다.");
        }

        return new CollaborationSnapshot(
                collaborationScore,
                conversationBalanceScore,
                inactiveMemberCount,
                dominantMemberCount,
                riskSignals
        );
    }

    private List<String> buildStrengthSignals(Team team, List<TeamMember> members, CollaborationSnapshot collaboration) {
        List<String> strengths = new ArrayList<>();
        if (team.getMatchingSummary() != null && !team.getMatchingSummary().isBlank()) {
            strengths.add(team.getMatchingSummary());
        }
        if (team.getProfileDiversityScore() >= 75) {
            strengths.add("서로 다른 학습 스타일이 섞여 있어 역할 분담 가능성이 높습니다.");
        }
        if (collaboration.conversationBalanceScore() >= 70) {
            strengths.add("팀 대화가 비교적 고르게 분산되어 있습니다.");
        }
        if (members.stream().anyMatch(member -> member.getLearningStyle() == TeamLearningStyle.FACILITATOR)) {
            strengths.add("조율형 팀원이 포함돼 의견 정리가 수월할 가능성이 높습니다.");
        }
        return strengths;
    }

    private List<TeamStyleDistributionResponse> buildStyleDistributions(List<TeamMember> members) {
        Map<TeamLearningStyle, Integer> counts = new EnumMap<>(TeamLearningStyle.class);
        for (TeamMember member : members) {
            counts.merge(member.getLearningStyle(), 1, Integer::sum);
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new TeamStyleDistributionResponse(entry.getKey().name(), entry.getValue()))
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
        if (!courseInstructorRepository.existsByInstructorIdAndCourseId(user.getId(), courseId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의만 관리할 수 있습니다.");
        }
        return course;
    }

    private Course getAccessibleCourse(Long courseId, User user) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "강의를 찾을 수 없습니다."));
        if (user.getRole() == UserRole.ADMIN) {
            return course;
        }
        if (user.getRole() == UserRole.INSTRUCTOR) {
            if (!courseInstructorRepository.existsByInstructorIdAndCourseId(user.getId(), courseId)) {
                throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의만 접근할 수 있습니다.");
            }
            return course;
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
            if (!courseInstructorRepository.existsByInstructorIdAndCourseId(user.getId(), team.getCourse().getId())) {
                throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의의 팀만 조회할 수 있습니다.");
            }
            return;
        }
        if (!teamMemberRepository.existsByTeamIdAndUserIdAndStatus(team.getId(), user.getId(), TeamMemberStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "소속된 팀만 조회할 수 있습니다.");
        }
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
        if (!teamMemberRepository.existsByTeamIdAndUserIdAndStatus(teamId, user.getId(), TeamMemberStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "소속된 팀 채팅방에만 메시지를 보낼 수 있습니다.");
        }
    }

    private Map<Long, Long> loadMemberCounts(List<Team> teams) {
        if (teams.isEmpty()) {
            return Map.of();
        }
        List<Long> teamIds = teams.stream().map(Team::getId).toList();
        Map<Long, Long> counts = new HashMap<>();
        for (TeamMemberCountView countView : teamMemberRepository.countByTeamIdsAndStatus(teamIds, TeamMemberStatus.ACTIVE)) {
            counts.put(countView.getTeamId(), countView.getMemberCount());
        }
        return counts;
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

    private int average(List<Integer> values) {
        if (values.isEmpty()) {
            return 0;
        }
        return clamp((int) Math.round(values.stream().mapToInt(Integer::intValue).average().orElse(0.0)));
    }

    private int spread(List<Integer> values) {
        if (values.isEmpty()) {
            return 0;
        }
        int min = values.stream().mapToInt(Integer::intValue).min().orElse(0);
        int max = values.stream().mapToInt(Integer::intValue).max().orElse(0);
        return clamp(max - min);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private record TeamDraft(List<LearnerProfile> profiles) {
    }

    private record LearnerProfile(
            User user,
            TeamLearningStyle learningStyle,
            int reliabilityScore,
            int initiativeScore,
            int supportScore,
            int understandingScore,
            String profileSummary
    ) {
        private int overallScore() {
            return (reliabilityScore + initiativeScore + supportScore + understandingScore) / 4;
        }
    }

    private record TeamMatching(
            int teamBuildingScore,
            int profileDiversityScore,
            String matchingSummary
    ) {
    }

    private record CollaborationSnapshot(
            int collaborationScore,
            int conversationBalanceScore,
            int inactiveMemberCount,
            int dominantMemberCount,
            List<String> riskSignals
    ) {
    }
}
