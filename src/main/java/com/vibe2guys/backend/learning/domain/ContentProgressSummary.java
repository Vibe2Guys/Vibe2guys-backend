package com.vibe2guys.backend.learning.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import com.vibe2guys.backend.course.domain.Content;
import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "content_progress_summaries",
        uniqueConstraints = @UniqueConstraint(name = "uk_content_progress_content_student", columnNames = {"content_id", "student_user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentProgressSummary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @Column(name = "watched_seconds", nullable = false)
    private int watchedSeconds;

    @Column(name = "total_seconds", nullable = false)
    private int totalSeconds;

    @Column(name = "progress_rate", nullable = false)
    private int progressRate;

    @Column(name = "last_position_seconds", nullable = false)
    private int lastPositionSeconds;

    @Column(name = "replay_count", nullable = false)
    private int replayCount;

    @Column(name = "is_completed", nullable = false)
    private boolean completed;

    @Column(name = "last_event_type", length = 50)
    private String lastEventType;

    @Builder
    private ContentProgressSummary(
            Course course,
            Content content,
            User student,
            int watchedSeconds,
            int totalSeconds,
            int progressRate,
            int lastPositionSeconds,
            int replayCount,
            boolean completed,
            String lastEventType
    ) {
        this.course = course;
        this.content = content;
        this.student = student;
        this.watchedSeconds = watchedSeconds;
        this.totalSeconds = totalSeconds;
        this.progressRate = progressRate;
        this.lastPositionSeconds = lastPositionSeconds;
        this.replayCount = replayCount;
        this.completed = completed;
        this.lastEventType = lastEventType;
    }

    public void updateProgress(
            int watchedSeconds,
            int totalSeconds,
            int progressRate,
            int lastPositionSeconds,
            int replayCount,
            boolean completed,
            String lastEventType
    ) {
        this.watchedSeconds = watchedSeconds;
        this.totalSeconds = totalSeconds;
        this.progressRate = progressRate;
        this.lastPositionSeconds = lastPositionSeconds;
        this.replayCount = replayCount;
        this.completed = completed;
        this.lastEventType = lastEventType;
    }
}
