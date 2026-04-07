package com.vibe2guys.backend.learning.service;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.course.domain.Content;
import com.vibe2guys.backend.course.domain.CourseEnrollment;
import com.vibe2guys.backend.course.repository.ContentRepository;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.learning.domain.ContentProgressSummary;
import com.vibe2guys.backend.learning.domain.LearningEvent;
import com.vibe2guys.backend.learning.domain.LearningEventType;
import com.vibe2guys.backend.learning.dto.ContentProgressRequest;
import com.vibe2guys.backend.learning.dto.ContentProgressResponse;
import com.vibe2guys.backend.learning.repository.ContentProgressSummaryRepository;
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
public class ContentProgressService {

    private final ContentRepository contentRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final ContentProgressSummaryRepository contentProgressSummaryRepository;
    private final LearningEventRepository learningEventRepository;
    private final UserService userService;

    @Transactional
    public ContentProgressResponse saveProgress(Long contentId, Long userId, ContentProgressRequest request) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "학생만 학습 진도를 기록할 수 있습니다.");
        }

        Content content = getAccessibleContent(contentId, userId);
        ContentProgressSummary summary = contentProgressSummaryRepository.findByContentIdAndStudentId(contentId, userId)
                .orElseGet(() -> ContentProgressSummary.builder()
                        .course(content.getCourse())
                        .content(content)
                        .student(user)
                        .watchedSeconds(0)
                        .totalSeconds(request.totalSeconds())
                        .progressRate(0)
                        .lastPositionSeconds(0)
                        .replayCount(0)
                        .completed(false)
                        .build());

        boolean completed = request.progressRate() >= 100;
        summary.updateProgress(
                request.watchedSeconds(),
                request.totalSeconds(),
                request.progressRate(),
                request.lastPositionSeconds(),
                request.replayCount(),
                completed,
                request.eventType()
        );
        contentProgressSummaryRepository.save(summary);

        learningEventRepository.save(LearningEvent.builder()
                .eventType(resolveEventType(request.eventType()))
                .actor(user)
                .course(content.getCourse())
                .week(content.getWeek())
                .content(content)
                .resourceType("CONTENT")
                .resourceId(content.getId())
                .occurredAt(OffsetDateTime.now())
                .payloadJson(buildPayload(request))
                .schemaVersion(1)
                .build());

        return ContentProgressResponse.from(summary);
    }

    public ContentProgressResponse getProgress(Long contentId, Long userId) {
        getAccessibleContent(contentId, userId);
        ContentProgressSummary summary = contentProgressSummaryRepository.findByContentIdAndStudentId(contentId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_PROGRESS_NOT_FOUND, "저장된 학습 진도가 없습니다."));
        return ContentProgressResponse.from(summary);
    }

    private Content getAccessibleContent(Long contentId, Long userId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠를 찾을 수 없습니다."));

        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseIdAndStudentId(content.getCourse().getId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 강의의 콘텐츠만 접근할 수 있습니다."));

        if (enrollment.getStatus() != com.vibe2guys.backend.course.domain.EnrollmentStatus.ENROLLED) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "활성 수강 상태가 아닙니다.");
        }
        return content;
    }

    private LearningEventType resolveEventType(String eventType) {
        return switch (eventType.toUpperCase()) {
            case "PAUSE" -> LearningEventType.PLAYBACK_PAUSE;
            case "SEEK" -> LearningEventType.PLAYBACK_SEEK;
            case "ENDED", "COMPLETE", "COMPLETED" -> LearningEventType.PLAYBACK_COMPLETE;
            default -> LearningEventType.CONTENT_PROGRESS;
        };
    }

    private Map<String, Object> buildPayload(ContentProgressRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("watchedSeconds", request.watchedSeconds());
        payload.put("totalSeconds", request.totalSeconds());
        payload.put("progressRate", request.progressRate());
        payload.put("lastPositionSeconds", request.lastPositionSeconds());
        payload.put("replayCount", request.replayCount());
        payload.put("stoppedSegmentStart", request.stoppedSegmentStart());
        payload.put("stoppedSegmentEnd", request.stoppedSegmentEnd());
        payload.put("eventType", request.eventType());
        return payload;
    }
}
