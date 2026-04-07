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
import com.vibe2guys.backend.learning.dto.ContentProgressEventType;
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
        validateProgressRequest(content, request);

        int totalSeconds = content.getDurationSeconds() != null ? content.getDurationSeconds() : request.totalSeconds();
        int progressRate = calculateProgressRate(request.watchedSeconds(), totalSeconds);
        boolean completed = progressRate >= 100;

        ContentProgressSummary summary = contentProgressSummaryRepository.findByContentIdAndStudentId(contentId, userId)
                .orElseGet(() -> ContentProgressSummary.builder()
                        .course(content.getCourse())
                        .content(content)
                        .student(user)
                        .watchedSeconds(0)
                        .totalSeconds(totalSeconds)
                        .progressRate(0)
                        .lastPositionSeconds(0)
                        .replayCount(0)
                        .completed(false)
                        .build());

        summary.updateProgress(
                request.watchedSeconds(),
                totalSeconds,
                progressRate,
                request.lastPositionSeconds(),
                request.replayCount(),
                completed,
                request.eventType().name()
        );
        contentProgressSummaryRepository.save(summary);

        learningEventRepository.save(LearningEvent.builder()
                .eventType(resolveEventType(request.eventType().name()))
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
        return switch (ContentProgressEventType.valueOf(eventType.toUpperCase())) {
            case PAUSE -> LearningEventType.PLAYBACK_PAUSE;
            case SEEK -> LearningEventType.PLAYBACK_SEEK;
            case ENDED -> LearningEventType.PLAYBACK_COMPLETE;
            case PROGRESS -> LearningEventType.CONTENT_PROGRESS;
        };
    }

    private void validateProgressRequest(Content content, ContentProgressRequest request) {
        int effectiveTotalSeconds = content.getDurationSeconds() != null ? content.getDurationSeconds() : request.totalSeconds();
        if (request.totalSeconds() != effectiveTotalSeconds) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "콘텐츠 길이 정보가 올바르지 않습니다.");
        }
        if (request.watchedSeconds() > effectiveTotalSeconds) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "watchedSeconds는 전체 길이를 초과할 수 없습니다.");
        }
        if (request.lastPositionSeconds() > effectiveTotalSeconds) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "lastPositionSeconds는 전체 길이를 초과할 수 없습니다.");
        }
        if (request.stoppedSegmentStart() != null && request.stoppedSegmentStart() < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "stoppedSegmentStart는 0 이상이어야 합니다.");
        }
        if (request.stoppedSegmentEnd() != null && request.stoppedSegmentEnd() < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "stoppedSegmentEnd는 0 이상이어야 합니다.");
        }
        if (request.stoppedSegmentStart() != null && request.stoppedSegmentEnd() != null
                && request.stoppedSegmentStart() > request.stoppedSegmentEnd()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "재생 구간 시작은 종료보다 클 수 없습니다.");
        }
    }

    private int calculateProgressRate(int watchedSeconds, int totalSeconds) {
        if (totalSeconds <= 0) {
            return 0;
        }
        return Math.min(100, (int) Math.floor((watchedSeconds * 100.0) / totalSeconds));
    }

    private Map<String, Object> buildPayload(ContentProgressRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("watchedSeconds", request.watchedSeconds());
        payload.put("totalSeconds", request.totalSeconds());
        payload.put("lastPositionSeconds", request.lastPositionSeconds());
        payload.put("replayCount", request.replayCount());
        payload.put("stoppedSegmentStart", request.stoppedSegmentStart());
        payload.put("stoppedSegmentEnd", request.stoppedSegmentEnd());
        payload.put("eventType", request.eventType().name());
        return payload;
    }
}
