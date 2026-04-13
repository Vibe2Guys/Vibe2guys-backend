package com.vibe2guys.backend.course.service;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.common.response.PageResponse;
import com.vibe2guys.backend.assignment.domain.Assignment;
import com.vibe2guys.backend.assignment.domain.AssignmentSubmission;
import com.vibe2guys.backend.assignment.repository.AssignmentRepository;
import com.vibe2guys.backend.assignment.repository.AssignmentSubmissionRepository;
import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.course.domain.CourseAnnouncement;
import com.vibe2guys.backend.course.domain.Content;
import com.vibe2guys.backend.course.domain.CourseStatus;
import com.vibe2guys.backend.course.domain.CourseEnrollment;
import com.vibe2guys.backend.course.domain.CourseInstructor;
import com.vibe2guys.backend.course.domain.CourseStudentMemo;
import com.vibe2guys.backend.course.domain.CourseWeek;
import com.vibe2guys.backend.course.domain.EnrollmentStatus;
import com.vibe2guys.backend.course.dto.ContentDetailResponse;
import com.vibe2guys.backend.course.dto.CourseAnnouncementResponse;
import com.vibe2guys.backend.course.dto.CourseLearningLogItemResponse;
import com.vibe2guys.backend.course.dto.CourseListItemResponse;
import com.vibe2guys.backend.course.dto.CourseStudentItemResponse;
import com.vibe2guys.backend.course.dto.CourseDetailResponse;
import com.vibe2guys.backend.course.dto.CourseGradeItemResponse;
import com.vibe2guys.backend.course.dto.CourseGradebookResponse;
import com.vibe2guys.backend.course.dto.CourseHomeResponse;
import com.vibe2guys.backend.course.dto.CourseHomeTodoItemResponse;
import com.vibe2guys.backend.course.dto.CourseInstructorSummaryResponse;
import com.vibe2guys.backend.course.dto.CourseWeekSummaryResponse;
import com.vibe2guys.backend.course.dto.CreateContentRequest;
import com.vibe2guys.backend.course.dto.CreateCourseAnnouncementRequest;
import com.vibe2guys.backend.course.dto.CreateContentResponse;
import com.vibe2guys.backend.course.dto.CreateCourseRequest;
import com.vibe2guys.backend.course.dto.CreateCourseResponse;
import com.vibe2guys.backend.course.dto.CreateWeekRequest;
import com.vibe2guys.backend.course.dto.CreateWeekResponse;
import com.vibe2guys.backend.course.dto.EnrollmentResponse;
import com.vibe2guys.backend.course.dto.MyCourseItemResponse;
import com.vibe2guys.backend.course.dto.UpdateCourseStudentMemoRequest;
import com.vibe2guys.backend.course.dto.UpdateCourseRequest;
import com.vibe2guys.backend.course.dto.UpdateCourseResponse;
import com.vibe2guys.backend.course.dto.WeekContentItemResponse;
import com.vibe2guys.backend.course.repository.CourseAnnouncementRepository;
import com.vibe2guys.backend.course.repository.CourseStudentMemoRepository;
import com.vibe2guys.backend.course.repository.ContentRepository;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.course.repository.CourseInstructorRepository;
import com.vibe2guys.backend.course.repository.CourseRepository;
import com.vibe2guys.backend.course.repository.CourseWeekRepository;
import com.vibe2guys.backend.analytics.domain.DailyAnalyticsSnapshot;
import com.vibe2guys.backend.analytics.repository.DailyAnalyticsSnapshotRepository;
import com.vibe2guys.backend.analytics.domain.RiskLevel;
import com.vibe2guys.backend.learning.domain.AttendanceSummary;
import com.vibe2guys.backend.learning.domain.ContentProgressSummary;
import com.vibe2guys.backend.learning.repository.AttendanceSummaryRepository;
import com.vibe2guys.backend.learning.repository.ContentProgressSummaryRepository;
import com.vibe2guys.backend.notification.service.NotificationService;
import com.vibe2guys.backend.quiz.domain.Quiz;
import com.vibe2guys.backend.quiz.domain.QuizQuestion;
import com.vibe2guys.backend.quiz.domain.QuizSubmission;
import com.vibe2guys.backend.quiz.repository.QuizQuestionRepository;
import com.vibe2guys.backend.quiz.repository.QuizRepository;
import com.vibe2guys.backend.quiz.repository.QuizSubmissionRepository;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseWeekRepository courseWeekRepository;
    private final CourseStudentMemoRepository courseStudentMemoRepository;
    private final CourseAnnouncementRepository courseAnnouncementRepository;
    private final ContentRepository contentRepository;
    private final ContentProgressSummaryRepository contentProgressSummaryRepository;
    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final DailyAnalyticsSnapshotRepository dailyAnalyticsSnapshotRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public List<MyCourseItemResponse> getMyCourses(Long userId) {
        User user = userService.getById(userId);
        if (user.getRole() == UserRole.INSTRUCTOR) {
            return mapInstructorCourses(courseInstructorRepository.findByInstructorId(userId));
        }
        return mapStudentCourses(courseEnrollmentRepository.findByStudentId(userId));
    }

    public PageResponse<CourseListItemResponse> getCourses(Long userId, int page, int size, String keyword) {
        User user = userService.getById(userId);
        PageRequest pageRequest = PageRequest.of(normalizePage(page), normalizeSize(size));
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<Course> coursePage = resolveCoursePage(user, normalizedKeyword, pageRequest);
        List<Course> courses = coursePage.getContent();
        Map<Long, String> instructorNames = resolveInstructorNames(courses);
        Set<Long> enrolledCourseIds = resolveEnrolledCourseIds(user, courses);

        return PageResponse.from(coursePage.map(course -> CourseListItemResponse.of(
                        course,
                        instructorNames.getOrDefault(course.getId(), course.getCreatedBy().getName()),
                        enrolledCourseIds.contains(course.getId())
                )));
    }

    @Transactional
    public EnrollmentResponse enroll(Long courseId, Long userId) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "학생만 수강 신청할 수 있습니다.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "강의를 찾을 수 없습니다."));
        if (!course.isPublic()) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "비공개 강의는 강의 코드로만 등록할 수 있습니다.");
        }

        if (courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, userId).isPresent()) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_ENROLLED, "이미 수강 신청된 강의입니다.");
        }

        return createEnrollment(course, user);
    }

    @Transactional
    public EnrollmentResponse enrollByCode(String rawCourseCode, Long userId) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "학생만 강의 등록을 할 수 있습니다.");
        }
        String courseCode = normalizeKeyword(rawCourseCode).toUpperCase();
        if (courseCode.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "강의 코드를 입력해주세요.");
        }
        Course course = courseRepository.findByCourseCodeIgnoreCase(courseCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "일치하는 강의 코드를 찾을 수 없습니다."));
        if (courseEnrollmentRepository.findByCourseIdAndStudentId(course.getId(), userId).isPresent()) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_ENROLLED, "이미 수강 신청된 강의입니다.");
        }
        return createEnrollment(course, user);
    }

    private EnrollmentResponse createEnrollment(Course course, User user) {
        CourseEnrollment enrollment = courseEnrollmentRepository.save(CourseEnrollment.builder()
                .course(course)
                .student(user)
                .status(EnrollmentStatus.ENROLLED)
                .enrolledAt(OffsetDateTime.now())
                .build());

        return new EnrollmentResponse(enrollment.getCourse().getId(), enrollment.getStudent().getId(), enrollment.getStatus().name());
    }

    @Transactional
    public CreateCourseResponse createCourse(Long userId, CreateCourseRequest request) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.INSTRUCTOR && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "교수자 또는 관리자만 강의를 생성할 수 있습니다.");
        }
        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "종료일은 시작일보다 빠를 수 없습니다.");
        }

        Course course = courseRepository.save(Course.builder()
                .title(request.title())
                .description(request.description())
                .thumbnailUrl(request.thumbnailUrl())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .sequentialRelease(request.isSequentialRelease())
                .isPublic(request.isPublic())
                .courseCode(generateCourseCode())
                .status(CourseStatus.DRAFT)
                .createdBy(user)
                .build());

        if (user.getRole() == UserRole.INSTRUCTOR) {
            courseInstructorRepository.save(CourseInstructor.builder()
                    .course(course)
                    .instructor(user)
                    .build());
        }

        return CreateCourseResponse.from(course);
    }

    @Transactional
    public UpdateCourseResponse updateCourse(Long courseId, Long userId, UpdateCourseRequest request) {
        User user = userService.getById(userId);
        Course course = getManageableCourse(courseId, user);

        String title = resolveUpdatedText(request.title(), course.getTitle(), "title");
        String description = resolveUpdatedText(request.description(), course.getDescription(), "description");
        String thumbnailUrl = request.thumbnailUrl() != null ? request.thumbnailUrl().trim() : course.getThumbnailUrl();
        java.time.LocalDate startDate = request.startDate() != null ? request.startDate() : course.getStartDate();
        java.time.LocalDate endDate = request.endDate() != null ? request.endDate() : course.getEndDate();
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "종료일은 시작일보다 빠를 수 없습니다.");
        }
        boolean sequentialRelease = request.isSequentialRelease() != null ? request.isSequentialRelease() : course.isSequentialRelease();
        boolean isPublic = request.isPublic() != null ? request.isPublic() : course.isPublic();
        CourseStatus status = request.status() != null ? parseCourseStatus(request.status()) : course.getStatus();

        course.update(title, description, thumbnailUrl, startDate, endDate, sequentialRelease, isPublic, status);
        return UpdateCourseResponse.from(course);
    }

    public CourseDetailResponse getCourseDetail(Long courseId, Long userId) {
        User user = userService.getById(userId);
        Course course = getAccessibleCourse(courseId, user);
        List<CourseWeekSummaryResponse> weeks = courseWeekRepository.findByCourseIdOrderByWeekNumberAsc(courseId).stream()
                .map(CourseWeekSummaryResponse::from)
                .toList();
        CourseInstructorSummaryResponse instructor = courseInstructorRepository.findByCourseId(courseId).stream()
                .findFirst()
                .map(item -> CourseInstructorSummaryResponse.from(item.getInstructor()))
                .orElse(CourseInstructorSummaryResponse.from(course.getCreatedBy()));
        return CourseDetailResponse.of(course, instructor, weeks);
    }

    public CourseHomeResponse getCourseHome(Long courseId, Long userId) {
        User user = userService.getById(userId);
        Course course = getAccessibleCourse(courseId, user);
        CourseProgressSnapshot snapshot = buildCourseProgressSnapshot(courseId, userId);
        List<CourseAnnouncementResponse> announcements = courseAnnouncementRepository
                .findByCourseIdOrderByPinnedDescCreatedAtDesc(courseId)
                .stream()
                .limit(5)
                .map(CourseAnnouncementResponse::from)
                .toList();
        List<CourseHomeTodoItemResponse> todos = buildCourseTodos(courseId, userId);
        return new CourseHomeResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCourseCode(),
                course.isPublic(),
                resolveInstructorName(course),
                snapshot.progressRate(),
                snapshot.attendanceRate(),
                (int) todos.stream()
                        .filter(item -> !"완료".equals(item.status()) && !"채점 완료".equals(item.status()))
                        .count(),
                snapshot.recentLearningTitle(),
                snapshot.recentLearningAt(),
                announcements,
                todos,
                courseWeekRepository.findByCourseIdOrderByWeekNumberAsc(courseId).stream()
                        .map(CourseWeekSummaryResponse::from)
                        .toList()
        );
    }

    public PageResponse<CourseStudentItemResponse> getStudents(Long courseId, Long userId, int page, int size, String keyword) {
        User user = userService.getById(userId);
        getManageableCourse(courseId, user);

        PageRequest pageRequest = PageRequest.of(normalizePage(page), normalizeSize(size));
        Page<CourseEnrollment> enrollments = courseEnrollmentRepository.findStudentPageByCourseId(
                courseId,
                normalizeKeyword(keyword),
                pageRequest
        );
        int contentCount = Math.max(contentRepository.findByCourseIdOrderByIdAsc(courseId).size(), 1);
        List<Long> studentIds = enrollments.getContent().stream().map(item -> item.getStudent().getId()).toList();
        Map<Long, String> memoByStudentId = new HashMap<>();
        if (!studentIds.isEmpty()) {
            for (CourseStudentMemo memo : courseStudentMemoRepository.findByCourseIdAndStudentIdIn(courseId, studentIds)) {
                memoByStudentId.put(memo.getStudent().getId(), memo.getMemoText());
            }
        }
        return PageResponse.from(enrollments.map(enrollment -> toCourseStudentItemResponse(enrollment, contentCount, memoByStudentId.get(enrollment.getStudent().getId()))));
    }

    public List<CourseAnnouncementResponse> getAnnouncements(Long courseId, Long userId) {
        User user = userService.getById(userId);
        getAccessibleCourse(courseId, user);
        return courseAnnouncementRepository.findByCourseIdOrderByPinnedDescCreatedAtDesc(courseId).stream()
                .map(CourseAnnouncementResponse::from)
                .toList();
    }

    @Transactional
    public CourseAnnouncementResponse createAnnouncement(Long courseId, Long userId, CreateCourseAnnouncementRequest request) {
        User user = userService.getById(userId);
        Course course = getManageableCourse(courseId, user);
        CourseAnnouncement announcement = courseAnnouncementRepository.save(CourseAnnouncement.builder()
                .course(course)
                .title(request.title().trim())
                .body(request.body().trim())
                .pinned(request.pinned())
                .createdBy(user)
                .build());
        return CourseAnnouncementResponse.from(announcement);
    }

    @Transactional
    public CourseStudentItemResponse updateStudentMemo(
            Long courseId,
            Long studentId,
            Long userId,
            UpdateCourseStudentMemoRequest request
    ) {
        User user = userService.getById(userId);
        Course course = getManageableCourse(courseId, user);
        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 학생만 메모를 남길 수 있습니다."));
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 학생만 메모를 남길 수 있습니다.");
        }

        CourseStudentMemo memo = courseStudentMemoRepository.findByCourseIdAndStudentId(courseId, studentId)
                .orElseGet(() -> courseStudentMemoRepository.save(CourseStudentMemo.builder()
                        .course(course)
                        .student(enrollment.getStudent())
                        .memoText(null)
                        .build()));
        String normalizedMemo = request.memo() == null ? null : request.memo().trim();
        memo.updateMemoText(normalizedMemo == null || normalizedMemo.isBlank() ? null : normalizedMemo);
        return toCourseStudentItemResponse(enrollment, Math.max(contentRepository.findByCourseIdOrderByIdAsc(courseId).size(), 1), memo.getMemoText());
    }

    @Transactional
    public CreateWeekResponse createWeek(Long courseId, Long userId, CreateWeekRequest request) {
        User user = userService.getById(userId);
        Course course = getManageableCourse(courseId, user);
        if (courseWeekRepository.existsByCourseIdAndWeekNumber(courseId, request.weekNumber())) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "같은 주차 번호가 이미 존재합니다.");
        }

        CourseWeek week = courseWeekRepository.save(CourseWeek.builder()
                .course(course)
                .weekNumber(request.weekNumber())
                .title(request.title())
                .openAt(request.openAt())
                .build());
        return CreateWeekResponse.from(week);
    }

    @Transactional
    public CreateContentResponse createContent(Long weekId, Long userId, CreateContentRequest request) {
        User user = userService.getById(userId);
        CourseWeek week = courseWeekRepository.findById(weekId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEEK_NOT_FOUND, "주차를 찾을 수 없습니다."));
        getManageableCourse(week.getCourse().getId(), user);

        validateCreateContentRequest(request);
        Content content = contentRepository.save(Content.builder()
                .course(week.getCourse())
                .week(week)
                .type(request.type())
                .title(request.title())
                .description(request.description())
                .videoUrl(request.videoUrl())
                .documentUrl(request.documentUrl())
                .durationSeconds(request.durationSeconds())
                .scheduledAt(request.scheduledAt())
                .openAt(request.openAt())
                .published(true)
                .build());
        notificationService.notifyNewContent(content);
        return CreateContentResponse.from(content);
    }

    public ContentDetailResponse getContentDetail(Long contentId, Long userId) {
        User user = userService.getById(userId);
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠를 찾을 수 없습니다."));
        getAccessibleCourse(content.getCourse().getId(), user);
        if (user.getRole() == UserRole.STUDENT && !canStudentAccessContent(content, OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠를 찾을 수 없습니다.");
        }
        return ContentDetailResponse.from(content);
    }

    public List<WeekContentItemResponse> getWeekContents(Long courseId, Long weekId, Long userId) {
        User user = userService.getById(userId);
        Course course = getAccessibleCourse(courseId, user);
        CourseWeek week = courseWeekRepository.findById(weekId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEEK_NOT_FOUND, "주차를 찾을 수 없습니다."));
        if (!week.getCourse().getId().equals(course.getId())) {
            throw new BusinessException(ErrorCode.WEEK_NOT_FOUND, "해당 강의의 주차가 아닙니다.");
        }

        OffsetDateTime now = OffsetDateTime.now();
        return contentRepository.findByWeekIdOrderByIdAsc(weekId).stream()
                .filter(content -> canViewContent(user, content, now))
                .map(WeekContentItemResponse::from)
                .toList();
    }

    public List<CourseLearningLogItemResponse> getMyLearningLogs(Long courseId, Long userId) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "학생만 자신의 학습 로그를 조회할 수 있습니다.");
        }
        getAccessibleCourse(courseId, user);

        Map<Long, ContentProgressSummary> progressByContentId = new HashMap<>();
        for (ContentProgressSummary summary : contentProgressSummaryRepository.findByCourseIdAndStudentId(courseId, userId)) {
            progressByContentId.put(summary.getContent().getId(), summary);
        }

        Map<Long, AttendanceSummary> attendanceByContentId = new HashMap<>();
        for (AttendanceSummary summary : attendanceSummaryRepository.findByCourseIdAndStudentId(courseId, userId)) {
            attendanceByContentId.put(summary.getContent().getId(), summary);
        }

        return contentRepository.findByCourseIdOrderByIdAsc(courseId).stream()
                .filter(content -> canViewContent(user, content, OffsetDateTime.now()))
                .sorted(Comparator.comparing((Content content) -> content.getWeek() != null ? content.getWeek().getWeekNumber() : Integer.MAX_VALUE)
                        .thenComparing(Content::getId))
                .map(content -> {
                    ContentProgressSummary progress = progressByContentId.get(content.getId());
                    AttendanceSummary attendance = attendanceByContentId.get(content.getId());
                    return new CourseLearningLogItemResponse(
                            content.getId(),
                            content.getTitle(),
                            content.getType().name(),
                            progress != null ? progress.getProgressRate() : null,
                            progress != null ? progress.isCompleted() : null,
                            attendance != null ? attendance.getStatus().name() : null,
                            attendance != null ? attendance.getAttendanceMinutes() : null
                    );
                })
                .toList();
    }

    public CourseGradebookResponse getMyGradebook(Long courseId, Long userId) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "학생만 자신의 성적을 조회할 수 있습니다.");
        }
        Course course = getAccessibleCourse(courseId, user);
        CourseProgressSnapshot snapshot = buildCourseProgressSnapshot(courseId, userId);
        List<CourseGradeItemResponse> assignmentItems = buildAssignmentGradeItems(courseId, userId);
        List<CourseGradeItemResponse> quizItems = buildQuizGradeItems(courseId, userId);
        int assignmentAverage = averagePercent(assignmentItems);
        int quizAverage = averagePercent(quizItems);
        int overallScore = clamp((int) Math.round(
                snapshot.attendanceRate() * 0.2
                        + assignmentAverage * 0.4
                        + quizAverage * 0.25
                        + snapshot.progressRate() * 0.15
        ));
        return new CourseGradebookResponse(
                courseId,
                course.getTitle(),
                snapshot.attendanceRate(),
                snapshot.progressRate(),
                assignmentAverage,
                quizAverage,
                overallScore,
                assignmentItems,
                quizItems
        );
    }

    private List<MyCourseItemResponse> mapStudentCourses(List<CourseEnrollment> enrollments) {
        List<MyCourseItemResponse> responses = new ArrayList<>();
        for (CourseEnrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            CourseProgressSnapshot snapshot = buildCourseProgressSnapshot(course.getId(), enrollment.getStudent().getId());
            responses.add(new MyCourseItemResponse(
                    course.getId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getThumbnailUrl(),
                    null,
                    course.getCourseCode(),
                    course.isPublic(),
                    snapshot.progressRate(),
                    snapshot.attendanceRate(),
                    countPendingAssignments(course.getId(), enrollment.getStudent().getId())
            ));
        }
        return responses;
    }

    private List<MyCourseItemResponse> mapInstructorCourses(List<CourseInstructor> instructors) {
        List<MyCourseItemResponse> responses = new ArrayList<>();
        for (CourseInstructor instructor : instructors) {
            Course course = instructor.getCourse();
            responses.add(new MyCourseItemResponse(
                    course.getId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getThumbnailUrl(),
                    instructor.getInstructor().getName(),
                    course.getCourseCode(),
                    course.isPublic(),
                    0,
                    0,
                    0
            ));
        }
        return responses;
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

        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 강의만 접근할 수 있습니다."));
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "활성 수강 상태가 아닙니다.");
        }
        return course;
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

    private void validateCreateContentRequest(CreateContentRequest request) {
        if (request.type() == com.vibe2guys.backend.course.domain.ContentType.VOD) {
            if (request.videoUrl() == null || request.videoUrl().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "동영상 콘텐츠는 영상 파일 업로드가 필요합니다.");
            }
        }
        if (request.type() == com.vibe2guys.backend.course.domain.ContentType.DOCUMENT) {
            if (request.documentUrl() == null || request.documentUrl().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "문서 콘텐츠는 문서 파일 업로드가 필요합니다.");
            }
        }
        if (request.type() == com.vibe2guys.backend.course.domain.ContentType.LIVE && request.scheduledAt() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "LIVE 콘텐츠는 scheduledAt이 필요합니다.");
        }
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private Page<Course> resolveCoursePage(User user, String keyword, PageRequest pageRequest) {
        if (user.getRole() == UserRole.INSTRUCTOR || user.getRole() == UserRole.ADMIN) {
            return keyword.isBlank()
                    ? courseRepository.findAll(pageRequest)
                    : courseRepository.findByTitleContainingIgnoreCase(keyword, pageRequest);
        }
        return keyword.isBlank()
                ? courseRepository.findByIsPublicTrue(pageRequest)
                : courseRepository.findByIsPublicTrueAndTitleContainingIgnoreCase(keyword, pageRequest);
    }

    private CourseStudentItemResponse toCourseStudentItemResponse(CourseEnrollment enrollment, int contentCount, String memo) {
        Long courseId = enrollment.getCourse().getId();
        Long studentId = enrollment.getStudent().getId();
        List<ContentProgressSummary> progressSummaries = contentProgressSummaryRepository.findByCourseIdAndStudentId(courseId, studentId);
        List<AttendanceSummary> attendanceSummaries = attendanceSummaryRepository.findByCourseIdAndStudentId(courseId, studentId);
        int progressRate = progressSummaries.isEmpty()
                ? 0
                : averageScore(progressSummaries.stream().map(ContentProgressSummary::getProgressRate).toList());
        int attendanceRate = clamp(attendanceSummaries.size() * 100 / Math.max(contentCount, 1));
        DailyAnalyticsSnapshot latestSnapshot = dailyAnalyticsSnapshotRepository
                .findTopByStudentIdAndCourseIdOrderBySnapshotDateDesc(studentId, courseId)
                .orElse(null);
        int understandingScore = latestSnapshot != null ? latestSnapshot.getUnderstandingScore() : progressRate;
        RiskLevel riskLevel = latestSnapshot != null ? latestSnapshot.getRiskLevel() : resolveRiskLevel(progressRate, attendanceRate);
        String statusSummary = buildStudentStatusSummary(progressRate, attendanceRate, understandingScore, riskLevel);
        return CourseStudentItemResponse.of(
                enrollment,
                progressRate,
                attendanceRate,
                understandingScore,
                riskLevel.name(),
                statusSummary,
                memo
        );
    }

    private List<CourseHomeTodoItemResponse> buildCourseTodos(Long courseId, Long userId) {
        OffsetDateTime now = OffsetDateTime.now();
        List<CourseHomeTodoItemResponse> items = new ArrayList<>();

        for (Assignment assignment : assignmentRepository.findByCourseIdOrderByDueAtAsc(courseId)) {
            AssignmentSubmission submission = assignmentSubmissionRepository
                    .findByAssignmentIdAndStudentId(assignment.getId(), userId)
                    .orElse(null);
            String status = submission == null
                    ? (assignment.getDueAt().isBefore(now) ? "미제출" : "제출 필요")
                    : (submission.getScore() == null ? "제출 완료" : "채점 완료");
            items.add(new CourseHomeTodoItemResponse(
                    "ASSIGNMENT",
                    assignment.getId(),
                    assignment.getTitle(),
                    status,
                    assignment.getDueAt(),
                    "과제"
            ));
        }

        for (Quiz quiz : quizRepository.findByCourseIdOrderByDueAtAsc(courseId)) {
            QuizSubmission submission = quizSubmissionRepository.findByQuizIdAndStudentId(quiz.getId(), userId).orElse(null);
            String status = submission == null
                    ? (quiz.getDueAt().isBefore(now) ? "미응시" : "응시 필요")
                    : "응시 완료";
            items.add(new CourseHomeTodoItemResponse(
                    "QUIZ",
                    quiz.getId(),
                    quiz.getTitle(),
                    status,
                    quiz.getDueAt(),
                    "퀴즈"
            ));
        }

        for (Content content : contentRepository.findByCourseIdOrderByIdAsc(courseId)) {
            if (content.getOpenAt() != null && content.getOpenAt().isAfter(now)) {
                items.add(new CourseHomeTodoItemResponse(
                        "CONTENT",
                        content.getId(),
                        content.getTitle(),
                        "예정",
                        content.getOpenAt(),
                        "콘텐츠 오픈 예정"
                ));
            }
        }

        items.sort(Comparator.comparing(CourseHomeTodoItemResponse::scheduleAt, Comparator.nullsLast(Comparator.naturalOrder())));
        return items.stream().limit(8).toList();
    }

    private List<CourseGradeItemResponse> buildAssignmentGradeItems(Long courseId, Long userId) {
        List<CourseGradeItemResponse> items = new ArrayList<>();
        for (Assignment assignment : assignmentRepository.findByCourseIdOrderByDueAtAsc(courseId)) {
            AssignmentSubmission submission = assignmentSubmissionRepository
                    .findByAssignmentIdAndStudentId(assignment.getId(), userId)
                    .orElse(null);
            int earnedScore = submission != null && submission.getScore() != null ? submission.getScore() : 0;
            int maxScore = Math.max(assignment.getMaxScore(), 1);
            String status = submission == null ? "미제출" : submission.getScore() == null ? "채점 대기" : "채점 완료";
            items.add(new CourseGradeItemResponse(
                    "ASSIGNMENT",
                    assignment.getId(),
                    assignment.getTitle(),
                    earnedScore,
                    assignment.getMaxScore(),
                    clamp((int) Math.round(earnedScore * 100.0 / maxScore)),
                    status,
                    submission != null ? submission.getFeedbackText() : null,
                    submission != null ? submission.getSubmittedAt() : null,
                    assignment.getDueAt()
            ));
        }
        return items;
    }

    private List<CourseGradeItemResponse> buildQuizGradeItems(Long courseId, Long userId) {
        List<CourseGradeItemResponse> items = new ArrayList<>();
        for (Quiz quiz : quizRepository.findByCourseIdOrderByDueAtAsc(courseId)) {
            int maxScore = Math.max(
                    quizQuestionRepository.findByQuizIdOrderBySortOrderAsc(quiz.getId()).stream()
                            .mapToInt(QuizQuestion::getScore)
                            .sum(),
                    1
            );
            QuizSubmission submission = quizSubmissionRepository.findByQuizIdAndStudentId(quiz.getId(), userId).orElse(null);
            int earnedScore = submission != null ? submission.getTotalScore() : 0;
            String status = submission == null ? "미응시" : submission.getSubjectiveScore() == null ? "채점 대기" : "채점 완료";
            items.add(new CourseGradeItemResponse(
                    "QUIZ",
                    quiz.getId(),
                    quiz.getTitle(),
                    earnedScore,
                    maxScore,
                    clamp((int) Math.round(earnedScore * 100.0 / maxScore)),
                    status,
                    submission != null && submission.getSubjectiveScore() == null ? "주관식 채점 전입니다." : null,
                    submission != null ? submission.getSubmittedAt() : null,
                    quiz.getDueAt()
            ));
        }
        return items;
    }

    private int countPendingAssignments(Long courseId, Long studentId) {
        int pending = 0;
        for (Assignment assignment : assignmentRepository.findByCourseIdOrderByDueAtAsc(courseId)) {
            if (assignmentSubmissionRepository.findByAssignmentIdAndStudentId(assignment.getId(), studentId).isEmpty()) {
                pending++;
            }
        }
        return pending;
    }

    private CourseProgressSnapshot buildCourseProgressSnapshot(Long courseId, Long studentId) {
        List<ContentProgressSummary> progressSummaries = contentProgressSummaryRepository.findByCourseIdAndStudentId(courseId, studentId);
        List<AttendanceSummary> attendanceSummaries = attendanceSummaryRepository.findByCourseIdAndStudentId(courseId, studentId);
        int contentCount = Math.max(contentRepository.findByCourseIdOrderByIdAsc(courseId).size(), 1);
        int progressRate = progressSummaries.isEmpty()
                ? 0
                : averageScore(progressSummaries.stream().map(ContentProgressSummary::getProgressRate).toList());
        int attendanceRate = clamp(attendanceSummaries.size() * 100 / contentCount);
        ContentProgressSummary recentProgress = progressSummaries.stream()
                .max(Comparator.comparing(ContentProgressSummary::getUpdatedAt))
                .orElse(null);
        return new CourseProgressSnapshot(
                progressRate,
                attendanceRate,
                recentProgress != null ? recentProgress.getContent().getTitle() : null,
                recentProgress != null ? recentProgress.getUpdatedAt() : null
        );
    }

    private String resolveInstructorName(Course course) {
        return courseInstructorRepository.findByCourseId(course.getId()).stream()
                .findFirst()
                .map(item -> item.getInstructor().getName())
                .orElseGet(() -> course.getCreatedBy().getName());
    }

    private RiskLevel resolveRiskLevel(int progressRate, int attendanceRate) {
        if (progressRate < 45 || attendanceRate < 45) {
            return RiskLevel.HIGH;
        }
        if (progressRate < 70 || attendanceRate < 70) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private String buildStudentStatusSummary(int progressRate, int attendanceRate, int understandingScore, RiskLevel riskLevel) {
        if (riskLevel == RiskLevel.HIGH || understandingScore < 50) {
            return "주의 필요";
        }
        if (riskLevel == RiskLevel.MEDIUM || progressRate < 75 || attendanceRate < 75) {
            return "관찰 필요";
        }
        return "안정";
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private int averageScore(List<Integer> scores) {
        if (scores.isEmpty()) {
            return 0;
        }
        return clamp((int) Math.round(scores.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0)));
    }

    private int averagePercent(List<CourseGradeItemResponse> items) {
        if (items.isEmpty()) {
            return 0;
        }
        return clamp((int) Math.round(items.stream()
                .mapToInt(CourseGradeItemResponse::percentScore)
                .average()
                .orElse(0)));
    }

    private Map<Long, String> resolveInstructorNames(List<Course> courses) {
        Map<Long, String> instructorNames = new HashMap<>();
        if (courses.isEmpty()) {
            return instructorNames;
        }
        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        for (CourseInstructor instructor : courseInstructorRepository.findByCourseIdIn(courseIds)) {
            instructorNames.putIfAbsent(instructor.getCourse().getId(), instructor.getInstructor().getName());
        }
        for (Course course : courses) {
            instructorNames.putIfAbsent(course.getId(), course.getCreatedBy().getName());
        }
        return instructorNames;
    }

    private Set<Long> resolveEnrolledCourseIds(User user, List<Course> courses) {
        if (user.getRole() != UserRole.STUDENT || courses.isEmpty()) {
            return Set.of();
        }
        List<Long> courseIds = courses.stream().map(Course::getId).toList();
        Set<Long> enrolledCourseIds = new HashSet<>();
        for (CourseEnrollment enrollment : courseEnrollmentRepository.findByStudentIdAndCourseIdInAndStatus(
                user.getId(),
                courseIds,
                EnrollmentStatus.ENROLLED
        )) {
            enrolledCourseIds.add(enrollment.getCourse().getId());
        }
        return enrolledCourseIds;
    }

    private String resolveUpdatedText(String updatedValue, String currentValue, String fieldName) {
        if (updatedValue == null) {
            return currentValue;
        }
        String normalized = updatedValue.trim();
        if (normalized.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, fieldName + "은 비어 있을 수 없습니다.");
        }
        return normalized;
    }

    private CourseStatus parseCourseStatus(String status) {
        try {
            return CourseStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 강의 상태입니다.");
        }
    }

    private String generateCourseCode() {
        return "CRS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean canViewContent(User user, Content content, OffsetDateTime now) {
        if (user.getRole() == UserRole.INSTRUCTOR || user.getRole() == UserRole.ADMIN) {
            return true;
        }
        return canStudentAccessContent(content, now);
    }

    private boolean canStudentAccessContent(Content content, OffsetDateTime now) {
        boolean opened = content.getOpenAt() == null || !content.getOpenAt().isAfter(now);
        return content.isPublished() && opened;
    }

    private record CourseProgressSnapshot(
            int progressRate,
            int attendanceRate,
            String recentLearningTitle,
            OffsetDateTime recentLearningAt
    ) {
    }
}
