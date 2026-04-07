package com.vibe2guys.backend.learning.service;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.course.domain.Content;
import com.vibe2guys.backend.course.domain.ContentType;
import com.vibe2guys.backend.course.domain.CourseEnrollment;
import com.vibe2guys.backend.course.domain.EnrollmentStatus;
import com.vibe2guys.backend.course.repository.ContentRepository;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.learning.domain.AttendanceStatus;
import com.vibe2guys.backend.learning.domain.AttendanceSummary;
import com.vibe2guys.backend.learning.domain.LearningEvent;
import com.vibe2guys.backend.learning.domain.LearningEventType;
import com.vibe2guys.backend.learning.dto.AttendanceEndRequest;
import com.vibe2guys.backend.learning.dto.AttendanceResponse;
import com.vibe2guys.backend.learning.dto.AttendanceStartRequest;
import com.vibe2guys.backend.learning.repository.AttendanceSummaryRepository;
import com.vibe2guys.backend.learning.repository.LearningEventRepository;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final ContentRepository contentRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final LearningEventRepository learningEventRepository;
    private final UserService userService;

    @Transactional
    public AttendanceResponse startAttendance(Long contentId, Long userId, AttendanceStartRequest request) {
        User user = validateStudent(userId);
        Content content = getAccessibleLiveContent(contentId, userId);
        validateAttendanceStart(content, request);

        AttendanceSummary summary = attendanceSummaryRepository.findByContentIdAndStudentId(contentId, userId)
                .orElseGet(() -> AttendanceSummary.builder()
                        .course(content.getCourse())
                        .content(content)
                        .student(user)
                        .firstEnteredAt(request.enteredAt())
                        .attendanceMinutes(0)
                        .status(AttendanceStatus.PRESENT)
                        .build());

        summary.start(request.enteredAt());
        attendanceSummaryRepository.save(summary);
        saveAttendanceEvent(user, content, LearningEventType.ATTENDANCE_ENTER, Map.of("enteredAt", request.enteredAt().toString()), request.enteredAt());
        return AttendanceResponse.started(summary);
    }

    @Transactional
    public AttendanceResponse finishAttendance(Long contentId, Long userId, AttendanceEndRequest request) {
        User user = validateStudent(userId);
        Content content = getAccessibleLiveContent(contentId, userId);

        AttendanceSummary summary = attendanceSummaryRepository.findByContentIdAndStudentId(contentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND, "출석 시작 기록이 없습니다."));

        validateAttendanceEnd(summary, request);
        summary.finish(request.leftAt());
        saveAttendanceEvent(user, content, LearningEventType.ATTENDANCE_LEAVE, Map.of("leftAt", request.leftAt().toString()), request.leftAt());
        return AttendanceResponse.finished(summary);
    }

    private User validateStudent(Long userId) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "학생만 출석을 기록할 수 있습니다.");
        }
        return user;
    }

    private Content getAccessibleLiveContent(Long contentId, Long userId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠를 찾을 수 없습니다."));
        if (content.getType() != ContentType.LIVE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "출석은 LIVE 콘텐츠에서만 기록할 수 있습니다.");
        }

        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseIdAndStudentId(content.getCourse().getId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 강의의 라이브 콘텐츠만 접근할 수 있습니다."));
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "활성 수강 상태가 아닙니다.");
        }
        return content;
    }

    private void saveAttendanceEvent(User user, Content content, LearningEventType eventType, Map<String, Object> payload, OffsetDateTime occurredAt) {
        Map<String, Object> orderedPayload = new LinkedHashMap<>(payload);
        learningEventRepository.save(LearningEvent.builder()
                .eventType(eventType)
                .actor(user)
                .course(content.getCourse())
                .week(content.getWeek())
                .content(content)
                .resourceType("CONTENT")
                .resourceId(content.getId())
                .occurredAt(occurredAt)
                .payloadJson(orderedPayload)
                .schemaVersion(1)
                .build());
    }

    private void validateAttendanceStart(Content content, AttendanceStartRequest request) {
        if (content.getScheduledAt() != null && request.enteredAt().isBefore(content.getScheduledAt().minusHours(1))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "허용된 출석 시작 시간보다 너무 이릅니다.");
        }
    }

    private void validateAttendanceEnd(AttendanceSummary summary, AttendanceEndRequest request) {
        if (summary.getLastLeftAt() != null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 종료된 출석 기록입니다.");
        }
        if (request.leftAt().isBefore(summary.getFirstEnteredAt())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "퇴장 시간은 입장 시간보다 빠를 수 없습니다.");
        }
    }
}
