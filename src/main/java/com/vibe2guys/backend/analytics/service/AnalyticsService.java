package com.vibe2guys.backend.analytics.service;

import com.vibe2guys.backend.ai.domain.AiFollowUpAnalysis;
import com.vibe2guys.backend.ai.service.AiRiskAnalysisService;
import com.vibe2guys.backend.ai.service.AiRiskAssessment;
import com.vibe2guys.backend.ai.repository.AiFollowUpAnalysisRepository;
import com.vibe2guys.backend.admin.domain.AnalyticsConfig;
import com.vibe2guys.backend.admin.repository.AnalyticsConfigRepository;
import com.vibe2guys.backend.analytics.domain.DailyAnalyticsSnapshot;
import com.vibe2guys.backend.analytics.domain.InstructorIntervention;
import com.vibe2guys.backend.analytics.domain.RiskLevel;
import com.vibe2guys.backend.analytics.dto.CreateInstructorInterventionRequest;
import com.vibe2guys.backend.analytics.dto.FollowUpUnderstandingItemResponse;
import com.vibe2guys.backend.analytics.dto.InstructorDashboardResponse;
import com.vibe2guys.backend.analytics.dto.InstructorInterventionItemResponse;
import com.vibe2guys.backend.analytics.dto.InstructorRiskStudentItemResponse;
import com.vibe2guys.backend.analytics.dto.InstructorStudentDetailResponse;
import com.vibe2guys.backend.analytics.dto.InstructorUnderstandingLowStudentResponse;
import com.vibe2guys.backend.analytics.dto.MyReportResponse;
import com.vibe2guys.backend.analytics.dto.ScoreDistributionBucketResponse;
import com.vibe2guys.backend.analytics.dto.ScoreDistributionResponse;
import com.vibe2guys.backend.analytics.dto.StudentAiUnderstandingResponse;
import com.vibe2guys.backend.analytics.dto.StudentAssignmentProgressItemResponse;
import com.vibe2guys.backend.analytics.dto.StudentContentProgressItemResponse;
import com.vibe2guys.backend.analytics.dto.StudentCourseAnalyticsItemResponse;
import com.vibe2guys.backend.analytics.dto.StudentDashboardResponse;
import com.vibe2guys.backend.analytics.dto.StudentRecommendationsResponse;
import com.vibe2guys.backend.analytics.dto.StudentRiskResponse;
import com.vibe2guys.backend.analytics.dto.StudentScoresResponse;
import com.vibe2guys.backend.analytics.dto.StudentQuizProgressItemResponse;
import com.vibe2guys.backend.analytics.repository.InstructorInterventionRepository;
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
@Transactional
public class AnalyticsService {

    private static final String SCORING_VERSION = "rules-v1";

    private final DailyAnalyticsSnapshotRepository snapshotRepository;
    private final InstructorInterventionRepository instructorInterventionRepository;
    private final AiFollowUpAnalysisRepository aiFollowUpAnalysisRepository;
    private final AnalyticsConfigRepository analyticsConfigRepository;
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
    private final AiRiskAnalysisService aiRiskAnalysisService;

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

    public StudentAiUnderstandingResponse getStudentAiUnderstanding(Long studentId, Long requesterId) {
        User requester = userService.getById(requesterId);
        User student = userService.getById(studentId);
        List<DailyAnalyticsSnapshot> snapshots = resolveSnapshotsForStudent(student, requester);
        if (snapshots.isEmpty()) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "조회 가능한 이해도 분석이 없습니다.");
        }

        List<AiFollowUpAnalysis> analyses = resolveVisibleFollowUpAnalyses(student, requester, snapshots);
        DailyAnalyticsSnapshot overall = aggregateSnapshots(snapshots);
        List<String> strengths = buildUnderstandingStrengths(overall, analyses);
        List<String> gaps = buildUnderstandingGaps(overall, analyses);
        String summary = buildUnderstandingSummary(student.getName(), overall, strengths, gaps);

        return new StudentAiUnderstandingResponse(
                student.getId(),
                student.getName(),
                overall.getUnderstandingScore(),
                summary,
                strengths,
                gaps,
                analyses.stream().limit(5).map(FollowUpUnderstandingItemResponse::from).toList()
        );
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

    public MyReportResponse getMyReport(Long userId) {
        return new MyReportResponse(getStudentDashboard(userId));
    }

    public StudentRiskResponse getStudentRisk(Long studentId, Long requesterId) {
        User requester = userService.getById(requesterId);
        User student = userService.getById(studentId);
        List<DailyAnalyticsSnapshot> snapshots = resolveSnapshotsForStudent(student, requester);
        if (snapshots.isEmpty()) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "조회 가능한 위험도 스냅샷이 없습니다.");
        }
        return StudentRiskResponse.from(aggregateSnapshots(snapshots));
    }

    public StudentRecommendationsResponse getRecommendations(Long studentId, Long requesterId) {
        User requester = userService.getById(requesterId);
        User student = userService.getById(studentId);
        if (requester.getRole() == UserRole.STUDENT && !requester.getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "본인 추천만 조회할 수 있습니다.");
        }
        List<DailyAnalyticsSnapshot> snapshots = resolveSnapshotsForStudent(student, requester);
        if (snapshots.isEmpty()) {
            return new StudentRecommendationsResponse(List.of("학습 데이터가 쌓이면 맞춤 추천을 제공할 수 있습니다."));
        }
        DailyAnalyticsSnapshot overall = aggregateSnapshots(snapshots);
        return new StudentRecommendationsResponse(buildRecommendations(overall));
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

    public List<InstructorRiskStudentItemResponse> getRiskStudents(Long courseId, Long userId) {
        User user = userService.getById(userId);
        getManageableCourse(courseId, user);
        return ensureSnapshotsForCourse(courseId, LocalDate.now()).stream()
                .filter(snapshot -> snapshot.getRiskLevel() == RiskLevel.HIGH || snapshot.getRiskLevel() == RiskLevel.MEDIUM)
                .map(InstructorRiskStudentItemResponse::from)
                .toList();
    }

    public List<InstructorUnderstandingLowStudentResponse> getLowUnderstandingStudents(Long courseId, Long userId) {
        User user = userService.getById(userId);
        getManageableCourse(courseId, user);
        return ensureSnapshotsForCourse(courseId, LocalDate.now()).stream()
                .filter(snapshot -> snapshot.getUnderstandingScore() < 60)
                .map(InstructorUnderstandingLowStudentResponse::from)
                .toList();
    }

    public InstructorStudentDetailResponse getInstructorStudentDetail(Long courseId, Long studentId, Long userId) {
        User user = userService.getById(userId);
        Course course = getManageableCourse(courseId, user);
        User student = getCourseStudent(courseId, studentId);
        DailyAnalyticsSnapshot snapshot = ensureSnapshotForCourseStudent(courseId, studentId, LocalDate.now());

        Map<Long, ContentProgressSummary> progressByContentId = new HashMap<>();
        for (ContentProgressSummary summary : progressSummaryRepository.findByCourseIdAndStudentId(courseId, studentId)) {
            progressByContentId.put(summary.getContent().getId(), summary);
        }
        Map<Long, AttendanceSummary> attendanceByContentId = new HashMap<>();
        for (AttendanceSummary summary : attendanceSummaryRepository.findByCourseIdAndStudentId(courseId, studentId)) {
            attendanceByContentId.put(summary.getContent().getId(), summary);
        }
        Map<Long, AssignmentSubmission> submissionByAssignmentId = new HashMap<>();
        for (AssignmentSubmission submission : assignmentSubmissionRepository.findByCourseIdAndStudentId(courseId, studentId)) {
            submissionByAssignmentId.put(submission.getAssignment().getId(), submission);
        }
        Map<Long, QuizSubmission> submissionByQuizId = new HashMap<>();
        for (QuizSubmission submission : quizSubmissionRepository.findByCourseIdAndStudentId(courseId, studentId)) {
            submissionByQuizId.put(submission.getQuiz().getId(), submission);
        }

        List<StudentAssignmentProgressItemResponse> assignments = assignmentRepository.findByCourseIdOrderByDueAtAsc(courseId).stream()
                .map(assignment -> StudentAssignmentProgressItemResponse.of(assignment, submissionByAssignmentId.get(assignment.getId())))
                .toList();
        List<StudentQuizProgressItemResponse> quizzes = quizRepository.findByCourseIdOrderByDueAtAsc(courseId).stream()
                .map(quiz -> StudentQuizProgressItemResponse.of(quiz, submissionByQuizId.get(quiz.getId())))
                .toList();
        List<StudentContentProgressItemResponse> contents = contentRepository.findByCourseIdOrderByIdAsc(courseId).stream()
                .map(content -> StudentContentProgressItemResponse.of(
                        content,
                        progressByContentId.get(content.getId()),
                        attendanceByContentId.get(content.getId())
                ))
                .toList();

        return new InstructorStudentDetailResponse(
                student.getId(),
                student.getName(),
                course.getId(),
                course.getTitle(),
                StudentScoresResponse.from(snapshot),
                StudentRiskResponse.from(snapshot),
                buildStudentAiUnderstandingForCourse(student, courseId, snapshot),
                assignments,
                quizzes,
                contents,
                instructorInterventionRepository.findByCourseIdAndStudentIdOrderByCreatedAtDesc(courseId, studentId).stream()
                        .map(InstructorInterventionItemResponse::from)
                        .toList()
        );
    }

    public List<InstructorInterventionItemResponse> getInterventions(Long courseId, Long userId) {
        User user = userService.getById(userId);
        getManageableCourse(courseId, user);
        return instructorInterventionRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(InstructorInterventionItemResponse::from)
                .toList();
    }

    public InstructorInterventionItemResponse createIntervention(Long courseId, Long userId, CreateInstructorInterventionRequest request) {
        User instructor = userService.getById(userId);
        Course course = getManageableCourse(courseId, instructor);
        User student = getCourseStudent(courseId, request.studentId());

        List<String> resourceUrls = request.resourceUrls() == null ? List.of() : request.resourceUrls().stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();

        InstructorIntervention intervention = instructorInterventionRepository.save(InstructorIntervention.builder()
                .course(course)
                .student(student)
                .instructor(instructor)
                .type(request.type())
                .title(request.title().trim())
                .message(request.message().trim())
                .resourceUrls(resourceUrls)
                .build());
        return InstructorInterventionItemResponse.from(intervention);
    }

    public ScoreDistributionResponse getScoreDistribution(Long courseId, Long userId) {
        User user = userService.getById(userId);
        Course course = getManageableCourse(courseId, user);
        List<DailyAnalyticsSnapshot> snapshots = ensureSnapshotsForCourse(courseId, LocalDate.now());
        return new ScoreDistributionResponse(
                course.getId(),
                course.getTitle(),
                buildDistributionBuckets(snapshots.stream().map(DailyAnalyticsSnapshot::getUnderstandingScore).toList()),
                buildDistributionBuckets(snapshots.stream().map(DailyAnalyticsSnapshot::getDropoutRiskScore).toList())
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

    private List<AiFollowUpAnalysis> resolveVisibleFollowUpAnalyses(User student, User requester, List<DailyAnalyticsSnapshot> snapshots) {
        if (requester.getRole() == UserRole.ADMIN || requester.getId().equals(student.getId())) {
            return aiFollowUpAnalysisRepository.findByQuestionStudentIdOrderByAnalyzedAtDesc(student.getId());
        }
        List<AiFollowUpAnalysis> analyses = new ArrayList<>();
        for (DailyAnalyticsSnapshot snapshot : snapshots) {
            analyses.addAll(aiFollowUpAnalysisRepository.findByQuestionStudentIdAndQuestionCourseIdOrderByAnalyzedAtDesc(
                    student.getId(),
                    snapshot.getCourse().getId()
            ));
        }
        return analyses;
    }

    private User getCourseStudent(Long courseId, Long studentId) {
        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, studentId)
                .filter(item -> item.getStatus() == EnrollmentStatus.ENROLLED)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 학생만 조회할 수 있습니다."));
        return enrollment.getStudent();
    }

    private DailyAnalyticsSnapshot ensureSnapshotForCourseStudent(Long courseId, Long studentId, LocalDate snapshotDate) {
        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, studentId)
                .filter(item -> item.getStatus() == EnrollmentStatus.ENROLLED)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 학생만 조회할 수 있습니다."));
        return upsertSnapshot(enrollment.getStudent(), enrollment.getCourse(), snapshotDate);
    }

    private StudentAiUnderstandingResponse buildStudentAiUnderstandingForCourse(User student, Long courseId, DailyAnalyticsSnapshot snapshot) {
        List<AiFollowUpAnalysis> analyses = aiFollowUpAnalysisRepository
                .findByQuestionStudentIdAndQuestionCourseIdOrderByAnalyzedAtDesc(student.getId(), courseId);
        List<String> strengths = buildUnderstandingStrengths(snapshot, analyses);
        List<String> gaps = buildUnderstandingGaps(snapshot, analyses);
        return new StudentAiUnderstandingResponse(
                student.getId(),
                student.getName(),
                snapshot.getUnderstandingScore(),
                buildUnderstandingSummary(student.getName(), snapshot, strengths, gaps),
                strengths,
                gaps,
                analyses.stream().limit(5).map(FollowUpUnderstandingItemResponse::from).toList()
        );
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
        AnalyticsMetrics metrics = calculateMetrics(student, course, snapshotDate, getAnalyticsConfig());
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

    private AnalyticsMetrics calculateMetrics(User student, Course course, LocalDate snapshotDate, AnalyticsConfig analyticsConfig) {
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

        int weightedHealthScore = clamp((int) Math.round(
                (attendanceRate * analyticsConfig.getAttendanceWeight())
                        + (progressCoverage * analyticsConfig.getProgressWeight())
                        + (assignmentSubmitRate * analyticsConfig.getAssignmentWeight())
                        + (understandingScore * analyticsConfig.getQuizWeight())
                        + (collaborationScore * analyticsConfig.getTeamActivityWeight())
        ));
        int riskScore = clamp(100 - weightedHealthScore);
        RiskLevel riskLevel = resolveRiskLevel(riskScore, analyticsConfig);
        List<String> reasons = buildReasons(attendanceRate, assignmentSubmitRate, understandingScore, progressCoverage);
        Map<String, Object> evidenceWindow = new HashMap<>();
        evidenceWindow.put("snapshotDate", snapshotDate.toString());
        evidenceWindow.put("attendanceRate", attendanceRate);
        evidenceWindow.put("progressRate", progressCoverage);
        evidenceWindow.put("assignmentSubmitRate", assignmentSubmitRate);
        String coachingMessage = buildCoachingMessage(riskLevel, reasons);
        List<String> recommendations = buildRecommendations(riskScore, understandingScore, diligenceScore);

        Map<String, Object> aiInput = new HashMap<>();
        aiInput.put("studentId", student.getId());
        aiInput.put("studentName", student.getName());
        aiInput.put("courseId", course.getId());
        aiInput.put("courseTitle", course.getTitle());
        aiInput.put("snapshotDate", snapshotDate.toString());
        aiInput.put("attendanceRate", attendanceRate);
        aiInput.put("progressRate", progressCoverage);
        aiInput.put("assignmentSubmitRate", assignmentSubmitRate);
        aiInput.put("understandingScore", understandingScore);
        aiInput.put("engagementScore", engagementScore);
        aiInput.put("collaborationScore", collaborationScore);
        aiInput.put("ruleBasedRiskScore", riskScore);
        aiInput.put("ruleBasedRiskLevel", riskLevel.name());
        aiInput.put("ruleBasedReasons", reasons);
        AiRiskAssessment aiRiskAssessment = aiRiskAnalysisService.assessStudentRisk(aiInput);
        if (aiRiskAssessment != null) {
            riskScore = aiRiskAssessment.riskScore();
            riskLevel = aiRiskAssessment.riskLevel();
            reasons = aiRiskAssessment.reasons().isEmpty() ? reasons : aiRiskAssessment.reasons();
            coachingMessage = aiRiskAssessment.coachingMessage();
            recommendations = aiRiskAssessment.recommendations().isEmpty() ? recommendations : aiRiskAssessment.recommendations();
            evidenceWindow.put("aiRiskAssessmentEnabled", true);
        }
        evidenceWindow.put("aiRecommendations", recommendations);

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
                assignmentSubmitRate,
                recommendations
        );
    }

    private DailyAnalyticsSnapshot aggregateSnapshots(List<DailyAnalyticsSnapshot> snapshots) {
        DailyAnalyticsSnapshot base = snapshots.getFirst();
        AnalyticsConfig analyticsConfig = getAnalyticsConfig();
        int diligenceScore = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getDiligenceScore).toList());
        int understandingScore = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getUnderstandingScore).toList());
        int engagementScore = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getEngagementScore).toList());
        int collaborationScore = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getCollaborationScore).toList());
        int riskScore = averageScore(snapshots.stream().map(DailyAnalyticsSnapshot::getDropoutRiskScore).toList());
        RiskLevel riskLevel = resolveRiskLevel(riskScore, analyticsConfig);

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
                .coachingMessage(base.getCoachingMessage())
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

    private List<String> buildRecommendations(DailyAnalyticsSnapshot snapshot) {
        Object aiRecommendations = snapshot.getEvidenceWindow().get("aiRecommendations");
        if (aiRecommendations instanceof List<?> values) {
            List<String> result = values.stream()
                    .map(value -> value == null ? "" : value.toString().trim())
                    .filter(value -> !value.isBlank())
                    .toList();
            if (!result.isEmpty()) {
                return result;
            }
        }
        List<String> recommendations = new ArrayList<>();
        if (snapshot.getDropoutRiskScore() >= 70) {
            recommendations.add("이번 주에는 가장 진도가 낮은 강의부터 복습하세요.");
        }
        if (snapshot.getUnderstandingScore() < 60) {
            recommendations.add("최근 퀴즈에서 틀린 개념을 다시 정리하고 유사 문제를 풀어보세요.");
        }
        if (snapshot.getDiligenceScore() < 70) {
            recommendations.add("출석과 과제 제출 일정을 먼저 점검하세요.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("현재 학습 리듬을 유지하면서 다음 강의도 미리 확인해보세요.");
        }
        return recommendations;
    }

    private List<String> buildRecommendations(int riskScore, int understandingScore, int diligenceScore) {
        List<String> recommendations = new ArrayList<>();
        if (riskScore >= 70) {
            recommendations.add("이번 주에는 가장 진도가 낮은 강의부터 복습하세요.");
        }
        if (understandingScore < 60) {
            recommendations.add("최근 퀴즈에서 틀린 개념을 다시 정리하고 유사 문제를 풀어보세요.");
        }
        if (diligenceScore < 70) {
            recommendations.add("출석과 과제 제출 일정을 먼저 점검하세요.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("현재 학습 리듬을 유지하면서 다음 강의도 미리 확인해보세요.");
        }
        return recommendations;
    }

    private RiskLevel resolveRiskLevel(int riskScore, AnalyticsConfig analyticsConfig) {
        if (riskScore >= analyticsConfig.getRiskThresholdHigh()) {
            return RiskLevel.HIGH;
        }
        if (riskScore >= analyticsConfig.getRiskThresholdMedium()) {
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

    private AnalyticsConfig getAnalyticsConfig() {
        return analyticsConfigRepository.findById(1L)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "analytics config가 초기화되지 않았습니다."));
    }

    private List<String> buildUnderstandingStrengths(DailyAnalyticsSnapshot snapshot, List<AiFollowUpAnalysis> analyses) {
        List<String> strengths = new ArrayList<>();
        if (snapshot.getUnderstandingScore() >= 70) {
            strengths.add("퀴즈와 과제 기반 이해도 점수가 안정적입니다.");
        }
        if (analyses.stream().anyMatch(analysis -> analysis.getUnderstandingScore() >= 70)) {
            strengths.add("꼬리질문 답변에서 개념 연결이 잘 드러났습니다.");
        }
        if (strengths.isEmpty()) {
            strengths.add("기초 개념은 유지하고 있으나 추가 보강이 필요합니다.");
        }
        return strengths;
    }

    private List<String> buildUnderstandingGaps(DailyAnalyticsSnapshot snapshot, List<AiFollowUpAnalysis> analyses) {
        List<String> gaps = new ArrayList<>();
        if (snapshot.getUnderstandingScore() < 60) {
            gaps.add("퀴즈 또는 적용형 문항에서 이해도 저하가 보입니다.");
        }
        if (analyses.stream().anyMatch(analysis -> analysis.getUnderstandingScore() < 60)) {
            gaps.add("꼬리질문 답변에서 개념 간 연결이 약합니다.");
        }
        if (snapshot.getEngagementScore() < 60) {
            gaps.add("진도와 참여 흐름이 이해도에 영향을 주고 있습니다.");
        }
        if (gaps.isEmpty()) {
            gaps.add("현재 큰 이해도 공백은 보이지 않습니다.");
        }
        return gaps;
    }

    private String buildUnderstandingSummary(String studentName, DailyAnalyticsSnapshot snapshot, List<String> strengths, List<String> gaps) {
        if (snapshot.getUnderstandingScore() >= 70) {
            return studentName + " 학생은 핵심 개념 이해가 비교적 안정적이며, " + strengths.getFirst();
        }
        return studentName + " 학생은 현재 이해도 보강이 필요합니다. " + gaps.getFirst();
    }

    private List<ScoreDistributionBucketResponse> buildDistributionBuckets(List<Integer> scores) {
        int[] counts = new int[5];
        for (Integer score : scores) {
            int normalized = score == null ? 0 : clamp(score);
            int index = Math.min(4, normalized / 20);
            counts[index]++;
        }
        return List.of(
                new ScoreDistributionBucketResponse("0-19", counts[0]),
                new ScoreDistributionBucketResponse("20-39", counts[1]),
                new ScoreDistributionBucketResponse("40-59", counts[2]),
                new ScoreDistributionBucketResponse("60-79", counts[3]),
                new ScoreDistributionBucketResponse("80-100", counts[4])
        );
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
            int assignmentSubmitRate,
            List<String> recommendations
    ) {
    }
}
