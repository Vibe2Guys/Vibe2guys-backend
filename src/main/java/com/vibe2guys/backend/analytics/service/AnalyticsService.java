package com.vibe2guys.backend.analytics.service;

import com.vibe2guys.backend.analytics.domain.DailyAnalyticsSnapshot;
import com.vibe2guys.backend.analytics.domain.RiskLevel;
import com.vibe2guys.backend.analytics.dto.InstructorDashboardResponse;
import com.vibe2guys.backend.analytics.dto.InstructorRiskStudentItemResponse;
import com.vibe2guys.backend.analytics.dto.StudentCourseAnalyticsItemResponse;
import com.vibe2guys.backend.analytics.dto.StudentDashboardResponse;
import com.vibe2guys.backend.analytics.dto.StudentScoresResponse;
import com.vibe2guys.backend.analytics.repository.DailyAnalyticsSnapshotRepository;
import com.vibe2guys.backend.assignment.domain.Assignment;
import com.vibe2guys.backend.assignment.domain.AssignmentSubmission;
import com.vibe2guys.backend.assignment.repository.AssignmentRepository;
import com.vibe2guys.backend.assignment.repository.AssignmentSubmissionRepository;
import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.course.domain.CourseEnrollment;
import com.vibe2guys.backend.course.domain.CourseInstructor;
import com.vibe2guys.backend.course.domain.EnrollmentStatus;
import com.vibe2guys.backend.course.repository.ContentRepository;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.course.repository.CourseInstructorRepository;
import com.vibe2guys.backend.course.repository.CourseRepository;
import com.vibe2guys.backend.learning.domain.AttendanceSummary;
import com.vibe2guys.backend.learning.domain.ContentProgressSummary;
import com.vibe2guys.backend.learning.repository.AttendanceSummaryRepository;
import com.vibe2guys.backend.learning.repository.ContentProgressSummaryRepository;
import com.vibe2guys.backend.quiz.domain.Quiz;
import com.vibe2guys.backend.quiz.domain.QuizSubmission;
import com.vibe2guys.backend.quiz.repository.QuizRepository;
import com.vibe2guys.backend.quiz.repository.QuizSubmissionRepository;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final String SCORING_VERSION = "rules-v1";

    private final DailyAnalyticsSnapshotRepository snapshotRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final ContentRepository contentRepository;
    private final ContentProgressSummaryRepository progressSummaryRepository;
    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserService userService;

    public StudentScoresResponse getStudentScores(Long studentId, Long requesterId) {
        User requester = userService.getById(requesterId);
        User student = userService.getById(studentId);
        if (requester.getRole() == UserRole.STUDENT && !requester.getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "본인 점수만 조회할 수 있습니다.");
        }

        List<DailyAnalyticsSnapshot> snapshots = resolveSnapshotsForStudent(student, requester);
        if (snapshots.isEmpty()) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "조회 가능한 학습 스냅샷이 없습니다.");
        }
        return StudentScoresResponse.from(aggregateSnapshots(snapshots));
    }

    public StudentDashboardResponse getStudentDashboard(Long userId) {
        User student = userService.getById(userId);
        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "학습자만 대시보드를 조회할 수 있습니다.");
        }

        List<DailyAnalyticsSnapshot> snapshots = ensureSnapshotsForStudent(student.getId(), LocalDate.now());
        if (snapshots.isEmpty()) {
            return new StudentDashboardResponse(0, 0, 0, 0, 0, RiskLevel.LOW.name(), "아직 학습 데이터가 충분하지 않습니다.", List.of(), List.of());
        }

        DailyAnalyticsSnapshot overall = aggregateSnapshots(snapshots);
        List<String> todayTodos = buildTodayTodos(overall);
        List<StudentCourseAnalyticsItemResponse> courseItems = snapshots.stream()
                .map(StudentCourseAnalyticsItemResponse::from)
                .toList();

        return new StudentDashboardResponse(
                overall.getDiligenceScore(),
                averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getEngagementScore).toList()),
                extractAssignmentRate(overall),
                overall.getUnderstandingScore(),
                overall.getEngagementScore(),
                overall.getRiskLevel().name(),
                overall.getCoachingMessage(),
                todayTodos,
                courseItems
        );
    }

    public InstructorDashboardResponse getInstructorDashboard(Long courseId, Long userId) {
        User user = userService.getById(userId);
        Course course = getManageableCourse(courseId, user);
        List<DailyAnalyticsSnapshot> snapshots = ensureSnapshotsForCourse(courseId, LocalDate.now());

        int averageProgressRate = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getEngagementScore).toList());
        int averageUnderstandingScore = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getUnderstandingScore).toList());
        List<InstructorRiskStudentItemResponse> riskStudents = snapshots.stream()
                .filter(snapshot -> snapshot.getRiskLevel() == RiskLevel.HIGH)
                .map(InstructorRiskStudentItemResponse::from)
                .toList();

        return new InstructorDashboardResponse(
                course.getId(),
                course.getTitle(),
                averageProgressRate,
                averageUnderstandingScore,
                riskStudents.size(),
                snapshots.size(),
                riskStudents
        );
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * *")
    public void computeDailySnapshots() {
        LocalDate snapshotDate = LocalDate.now();
        for (Course course : courseRepository.findAll()) {
            for (CourseEnrollment enrollment : courseEnrollmentRepository.findByCourseIdAndStatus(course.getId(), EnrollmentStatus.ENROLLED)) {
                upsertSnapshot(enrollment.getStudent(), course, snapshotDate);
            }
        }
    }

    @Transactional
    public List<DailyAnalyticsSnapshot> ensureSnapshotsForStudent(Long studentId, LocalDate snapshotDate) {
        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ENROLLED);
        List<DailyAnalyticsSnapshot> snapshots = new ArrayList<>();
        for (CourseEnrollment enrollment : enrollments) {
            snapshots.add(upsertSnapshot(enrollment.getStudent(), enrollment.getCourse(), snapshotDate));
        }
        return snapshots;
    }

    @Transactional
    public List<DailyAnalyticsSnapshot> ensureSnapshotsForCourse(Long courseId, LocalDate snapshotDate) {
        List<DailyAnalyticsSnapshot> snapshots = new ArrayList<>();
        for (CourseEnrollment enrollment : courseEnrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ENROLLED)) {
            snapshots.add(upsertSnapshot(enrollment.getStudent(), enrollment.getCourse(), snapshotDate));
        }
        return snapshots;
    }

    private List<DailyAnalyticsSnapshot> resolveSnapshotsForStudent(User student, User requester) {
        LocalDate today = LocalDate.now();
        if (requester.getRole() == UserRole.ADMIN || requester.getId().equals(student.getId())) {
            return ensureSnapshotsForStudent(student.getId(), today);
        }

        List<DailyAnalyticsSnapshot> visible = new ArrayList<>();
        for (DailyAnalyticsSnapshot snapshot : ensureSnapshotsForStudent(student.getId(), today)) {
            boolean ownsCourse = courseInstructorRepository.findByInstructorId(requester.getId()).stream()
                    .anyMatch(item -> item.getCourse().getId().equals(snapshot.getCourse().getId()));
            if (ownsCourse) {
                visible.add(snapshot);
            }
        }
        return visible;
    }

    private Course getManageableCourse(Long courseId, User user) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "강의를 찾을 수 없습니다."));
        if (user.getRole() == UserRole.ADMIN) {
            return course;
        }
        if (user.getRole() != UserRole.INSTRUCTOR) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "교수자만 코스 대시보드를 조회할 수 있습니다.");
        }
        CourseInstructor instructor = courseInstructorRepository.findByInstructorId(user.getId()).stream()
                .filter(item -> item.getCourse().getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의만 조회할 수 있습니다."));
        return instructor.getCourse();
    }

    private DailyAnalyticsSnapshot upsertSnapshot(User student, Course course, LocalDate snapshotDate) {
        AnalyticsMetrics metrics = calculateMetrics(student, course, snapshotDate);
        DailyAnalyticsSnapshot snapshot = snapshotRepository.findByStudentIdAndCourseIdAndSnapshotDate(student.getId(), course.getId(), snapshotDate)
                .orElseGet(() -> DailyAnalyticsSnapshot.builder()
                        .student(student)
                        .course(course)
                        .snapshotDate(snapshotDate)
                        .diligenceScore(metrics.diligenceScore())
                        .understandingScore(metrics.understandingScore())
                        .engagementScore(metrics.engagementScore())
                        .collaborationScore(metrics.collaborationScore())
                        .dropoutRiskScore(metrics.riskScore())
                        .riskLevel(metrics.riskLevel())
                        .reasons(metrics.reasons())
                        .evidenceWindow(metrics.evidenceWindow())
                        .coachingMessage(metrics.coachingMessage())
                        .scoringVersion(SCORING_VERSION)
                        .computedAt(OffsetDateTime.now())
                        .build());

        snapshot.update(
                metrics.diligenceScore(),
                metrics.understandingScore(),
                metrics.engagementScore(),
                metrics.collaborationScore(),
                metrics.riskScore(),
                metrics.riskLevel(),
                metrics.reasons(),
                metrics.evidenceWindow(),
                metrics.coachingMessage(),
                SCORING_VERSION,
                OffsetDateTime.now()
        );
        return snapshotRepository.save(snapshot);
    }

    private AnalyticsMetrics calculateMetrics(User student, Course course, LocalDate snapshotDate) {
        List<ContentProgressSummary> progressSummaries = progressSummaryRepository.findByCourseIdAndStudentId(course.getId(), student.getId());
        List<AttendanceSummary> attendanceSummaries = attendanceSummaryRepository.findByCourseIdAndStudentId(course.getId(), student.getId());
        List<Assignment> assignments = assignmentRepository.findByCourseIdOrderByDueAtAsc(course.getId());
        List<Quiz> quizzes = quizRepository.findByCourseIdOrderByDueAtAsc(course.getId());

        int totalContents = contentRepository.findByCourseIdOrderByIdAsc(course.getId()).size();
        int completedContents = (int) progressSummaries.stream().filter(ContentProgressSummary::isCompleted).count();
        int averageProgressRate = averageScore(progressSummaries.stream().map(ContentProgressSummary::getProgressRate).toList());
        int progressCoverage = totalContents == 0 ? 0 : clamp((completedContents * 100) / totalContents);

        int liveContents = (int) contentRepository.findByCourseIdOrderByIdAsc(course.getId()).stream()
                .filter(content -> content.getType().name().equals("LIVE"))
                .count();
        int attendanceRate = liveContents == 0 ? 100 : clamp((attendanceSummaries.size() * 100) / liveContents);

        int submittedAssignments = 0;
        for (Assignment assignment : assignments) {
            if (assignmentSubmissionRepository.findByAssignmentIdAndStudentId(assignment.getId(), student.getId()).isPresent()) {
                submittedAssignments++;
            }
        }
        int assignmentSubmitRate = assignments.isEmpty() ? 100 : clamp((submittedAssignments * 100) / assignments.size());

        int diligenceScore = averageScore(List.of(attendanceRate, progressCoverage, assignmentSubmitRate));

        List<Integer> quizScores = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            quizSubmissionRepository.findByQuizIdAndStudentId(quiz.getId(), student.getId())
                    .ifPresent(submission -> quizScores.add(submission.getTotalScore()));
        }
        int understandingScore = quizScores.isEmpty() ? Math.max(averageProgressRate, 40) : averageScore(quizScores);
        int engagementScore = averageScore(List.of(averageProgressRate, attendanceRate));
        int collaborationScore = 50;

        int riskScore = clamp(100 - averageScore(List.of(diligenceScore, understandingScore, engagementScore)));
        RiskLevel riskLevel = resolveRiskLevel(riskScore);
        List<String> reasons = buildReasons(attendanceRate, assignmentSubmitRate, understandingScore, progressCoverage);
        Map<String, Object> evidenceWindow = new HashMap<>();
        evidenceWindow.put("snapshotDate", snapshotDate.toString());
        evidenceWindow.put("attendanceRate", attendanceRate);
        evidenceWindow.put("progressRate", progressCoverage);
        evidenceWindow.put("assignmentSubmitRate", assignmentSubmitRate);
        String coachingMessage = buildCoachingMessage(riskLevel, reasons);

        return new AnalyticsMetrics(
                diligenceScore,
                understandingScore,
                engagementScore,
                collaborationScore,
                riskScore,
                riskLevel,
                reasons,
                evidenceWindow,
                coachingMessage,
                assignmentSubmitRate
        );
    }

    private DailyAnalyticsSnapshot aggregateSnapshots(List<DailyAnalyticsSnapshot> snapshots) {
        DailyAnalyticsSnapshot base = snapshots.getFirst();
        int diligenceScore = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getDiligenceScore).toList());
        int understandingScore = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getUnderstandingScore).toList());
        int engagementScore = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getEngagementScore).toList());
        int collaborationScore = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getCollaborationScore).toList());
        int riskScore = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getDropoutRiskScore).toList());
        RiskLevel riskLevel = resolveRiskLevel(riskScore);

        return DailyAnalyticsSnapshot.builder()
                .student(base.getStudent())
                .course(base.getCourse())
                .snapshotDate(base.getSnapshotDate())
                .diligenceScore(diligenceScore)
                .understandingScore(understandingScore)
                .engagementScore(engagementScore)
                .collaborationScore(collaborationScore)
                .dropoutRiskScore(riskScore)
                .riskLevel(riskLevel)
                .reasons(base.getReasons())
                .evidenceWindow(base.getEvidenceWindow())
                .coachingMessage(buildCoachingMessage(riskLevel, base.getReasons()))
                .scoringVersion(SCORING_VERSION)
                .computedAt(OffsetDateTime.now())
                .build();
    }

    private int extractAssignmentRate(DailyAnalyticsSnapshot snapshot) {
        Object value = snapshot.getEvidenceWindow().get("assignmentSubmitRate");
        if (value instanceof Integer integer) {
            return integer;
        }
        return 0;
    }

    private List<String> buildTodayTodos(DailyAnalyticsSnapshot snapshot) {
        List<String> todos = new ArrayList<>();
        if (snapshot.getDropoutRiskScore() >= 70) {
            todos.add("오늘은 미완료 강의부터 1개 복습하세요.");
        }
        if (snapshot.getUnderstandingScore() < 60) {
            todos.add("최근 퀴즈와 과제에서 틀린 개념을 다시 정리하세요.");
        }
        if (todos.isEmpty()) {
            todos.add("현재 학습 흐름을 유지하세요.");
        }
        return todos;
    }

    private RiskLevel resolveRiskLevel(int riskScore) {
        if (riskScore >= 70) {
            return RiskLevel.HIGH;
        }
        if (riskScore >= 40) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private List<String> buildReasons(int attendanceRate, int assignmentSubmitRate, int understandingScore, int progressRate) {
        List<String> reasons = new ArrayList<>();
        if (attendanceRate < 70) {
            reasons.add("출석률이 낮습니다.");
        }
        if (assignmentSubmitRate < 70) {
            reasons.add("과제 제출률이 낮습니다.");
        }
        if (understandingScore < 60) {
            reasons.add("퀴즈 또는 이해도 점수가 낮습니다.");
        }
        if (progressRate < 70) {
            reasons.add("진도율이 낮습니다.");
        }
        if (reasons.isEmpty()) {
            reasons.add("전반적인 학습 상태가 안정적입니다.");
        }
        return reasons;
    }

    private String buildCoachingMessage(RiskLevel riskLevel, List<String> reasons) {
        return switch (riskLevel) {
            case HIGH -> "최근 학습 흐름이 흔들리고 있습니다. " + reasons.getFirst();
            case MEDIUM -> "조금 더 관리가 필요합니다. " + reasons.getFirst();
            case LOW -> "좋은 흐름을 유지하고 있습니다.";
        };
    }

    private int averageScore(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return 0;
        }
        int sum = values.stream().mapToInt(Integer::intValue).sum();
        return clamp(sum / values.size());
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private record AnalyticsMetrics(
            int diligenceScore,
            int understandingScore,
            int engagementScore,
            int collaborationScore,
            int riskScore,
            RiskLevel riskLevel,
            List<String> reasons,
            Map<String, Object> evidenceWindow,
            String coachingMessage,
            int assignmentSubmitRate
    ) {
    }
}
