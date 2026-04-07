package com.vibe2guys.backend.notification.service;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.course.domain.Content;
import com.vibe2guys.backend.course.domain.CourseEnrollment;
import com.vibe2guys.backend.course.domain.EnrollmentStatus;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.notification.domain.Notification;
import com.vibe2guys.backend.notification.domain.NotificationType;
import com.vibe2guys.backend.notification.dto.NotificationItemResponse;
import com.vibe2guys.backend.notification.dto.NotificationReadResponse;
import com.vibe2guys.backend.notification.repository.NotificationRepository;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final NotificationRepository notificationRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final UserService userService;

    public NotificationService(
            NotificationRepository notificationRepository,
            CourseEnrollmentRepository courseEnrollmentRepository,
            UserService userService
    ) {
        this.notificationRepository = notificationRepository;
        this.courseEnrollmentRepository = courseEnrollmentRepository;
        this.userService = userService;
    }

    public List<NotificationItemResponse> getMyNotifications(Long userId) {
        userService.getById(userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationItemResponse::from)
                .toList();
    }

    @Transactional
    public NotificationReadResponse readNotification(Long notificationId, Long userId) {
        userService.getById(userId);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND, "알림을 찾을 수 없습니다."));
        notification.markAsRead(OffsetDateTime.now());
        return NotificationReadResponse.from(notification);
    }

    @Transactional
    public void notifyNewContent(Content content) {
        String title = "새 학습 콘텐츠가 등록되었습니다.";
        String body = content.getOpenAt() == null
                ? "\"" + content.getTitle() + "\" 콘텐츠를 확인해보세요."
                : "\"" + content.getTitle() + "\" 콘텐츠가 " + DATE_TIME_FORMATTER.format(content.getOpenAt()) + "에 열립니다.";
        notifyEnrolledStudents(content.getCourse().getId(), NotificationType.NEW_CONTENT, title, body);
    }

    @Transactional
    public void notifyAssignmentCreated(Long courseId, String assignmentTitle) {
        notifyEnrolledStudents(
                courseId,
                NotificationType.ASSIGNMENT_CREATED,
                "새 과제가 등록되었습니다.",
                "\"" + assignmentTitle + "\" 과제를 확인하고 마감 일정을 체크하세요."
        );
    }

    @Transactional
    public void notifyFollowUpQuestion(User student, String questionText) {
        notificationRepository.save(Notification.builder()
                .user(student)
                .type(NotificationType.FOLLOW_UP_QUESTION)
                .title("AI 꼬리질문이 도착했습니다.")
                .content(questionText)
                .read(false)
                .build());
    }

    private void notifyEnrolledStudents(Long courseId, NotificationType type, String title, String content) {
        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ENROLLED);
        for (CourseEnrollment enrollment : enrollments) {
            notificationRepository.save(Notification.builder()
                    .user(enrollment.getStudent())
                    .type(type)
                    .title(title)
                    .content(content)
                    .read(false)
                    .build());
        }
    }
}
