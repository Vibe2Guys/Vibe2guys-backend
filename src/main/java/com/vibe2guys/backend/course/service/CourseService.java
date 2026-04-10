package com.vibe2guys.backend.course.service;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.common.response.PageResponse;
import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.course.domain.Content;
import com.vibe2guys.backend.course.domain.CourseStatus;
import com.vibe2guys.backend.course.domain.CourseEnrollment;
import com.vibe2guys.backend.course.domain.CourseInstructor;
import com.vibe2guys.backend.course.domain.CourseWeek;
import com.vibe2guys.backend.course.domain.EnrollmentStatus;
import com.vibe2guys.backend.course.dto.ContentDetailResponse;
import com.vibe2guys.backend.course.dto.CourseLearningLogItemResponse;
import com.vibe2guys.backend.course.dto.CourseListItemResponse;
import com.vibe2guys.backend.course.dto.CourseStudentItemResponse;
import com.vibe2guys.backend.course.dto.CourseDetailResponse;
import com.vibe2guys.backend.course.dto.CourseInstructorSummaryResponse;
import com.vibe2guys.backend.course.dto.CourseWeekSummaryResponse;
import com.vibe2guys.backend.course.dto.CreateContentRequest;
import com.vibe2guys.backend.course.dto.CreateContentResponse;
import com.vibe2guys.backend.course.dto.CreateCourseRequest;
import com.vibe2guys.backend.course.dto.CreateCourseResponse;
import com.vibe2guys.backend.course.dto.CreateWeekRequest;
import com.vibe2guys.backend.course.dto.CreateWeekResponse;
import com.vibe2guys.backend.course.dto.EnrollmentResponse;
import com.vibe2guys.backend.course.dto.MyCourseItemResponse;
import com.vibe2guys.backend.course.dto.UpdateCourseRequest;
import com.vibe2guys.backend.course.dto.UpdateCourseResponse;
import com.vibe2guys.backend.course.dto.WeekContentItemResponse;
import com.vibe2guys.backend.course.repository.ContentRepository;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.course.repository.CourseInstructorRepository;
import com.vibe2guys.backend.course.repository.CourseRepository;
import com.vibe2guys.backend.course.repository.CourseWeekRepository;
import com.vibe2guys.backend.learning.domain.AttendanceSummary;
import com.vibe2guys.backend.learning.domain.ContentProgressSummary;
import com.vibe2guys.backend.learning.repository.AttendanceSummaryRepository;
import com.vibe2guys.backend.learning.repository.ContentProgressSummaryRepository;
import com.vibe2guys.backend.notification.service.NotificationService;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseWeekRepository courseWeekRepository;
    private final ContentRepository contentRepository;
    private final ContentProgressSummaryRepository contentProgressSummaryRepository;
    private final AttendanceSummaryRepository attendanceSummaryRepository;
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
        Page<Course> coursePage = normalizedKeyword.isBlank()
                ? courseRepository.findAll(pageRequest)
                : courseRepository.findByTitleContainingIgnoreCase(normalizedKeyword, pageRequest);
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

        if (courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, userId).isPresent()) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_ENROLLED, "이미 수강 신청된 강의입니다.");
        }

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
        CourseStatus status = request.status() != null ? parseCourseStatus(request.status()) : course.getStatus();

        course.update(title, description, thumbnailUrl, startDate, endDate, sequentialRelease, status);
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

    public PageResponse<CourseStudentItemResponse> getStudents(Long courseId, Long userId, int page, int size, String keyword) {
        User user = userService.getById(userId);
        getManageableCourse(courseId, user);

        PageRequest pageRequest = PageRequest.of(normalizePage(page), normalizeSize(size));
        Page<CourseEnrollment> enrollments = courseEnrollmentRepository.findStudentPageByCourseId(
                courseId,
                normalizeKeyword(keyword),
                pageRequest
        );
        return PageResponse.from(enrollments.map(CourseStudentItemResponse::from));
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

    private List<MyCourseItemResponse> mapStudentCourses(List<CourseEnrollment> enrollments) {
        List<MyCourseItemResponse> responses = new ArrayList<>();
        for (CourseEnrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            responses.add(new MyCourseItemResponse(
                    course.getId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getThumbnailUrl(),
                    null,
                    0,
                    0,
                    0
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
            if (request.videoUrl() == null || request.videoUrl().isBlank() || request.durationSeconds() == null || request.durationSeconds() <= 0) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "VOD 콘텐츠는 videoUrl과 durationSeconds가 필요합니다.");
            }
        }
        if (request.type() == com.vibe2guys.backend.course.domain.ContentType.DOCUMENT) {
            if (request.documentUrl() == null || request.documentUrl().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "DOCUMENT 콘텐츠는 documentUrl이 필요합니다.");
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
}
