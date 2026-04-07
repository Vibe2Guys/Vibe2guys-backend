package com.vibe2guys.backend.course.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "contents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private CourseWeek week;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "open_at")
    private OffsetDateTime openAt;

    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Builder
    private Content(
            Course course,
            CourseWeek week,
            ContentType type,
            String title,
            String description,
            String videoUrl,
            String documentUrl,
            Integer durationSeconds,
            OffsetDateTime scheduledAt,
            OffsetDateTime openAt,
            boolean published
    ) {
        this.course = course;
        this.week = week;
        this.type = type;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.documentUrl = documentUrl;
        this.durationSeconds = durationSeconds;
        this.scheduledAt = scheduledAt;
        this.openAt = openAt;
        this.published = published;
    }
}
