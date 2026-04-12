package com.vibe2guys.backend.course.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import com.vibe2guys.backend.user.domain.User;
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

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_sequential_release", nullable = false)
    private boolean sequentialRelease;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "course_code", nullable = false, length = 20, unique = true)
    private String courseCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Builder
    private Course(
            String title,
            String description,
            String thumbnailUrl,
            LocalDate startDate,
            LocalDate endDate,
            boolean sequentialRelease,
            boolean isPublic,
            String courseCode,
            CourseStatus status,
            User createdBy
    ) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sequentialRelease = sequentialRelease;
        this.isPublic = isPublic;
        this.courseCode = courseCode;
        this.status = status;
        this.createdBy = createdBy;
    }

    public void update(
            String title,
            String description,
            String thumbnailUrl,
            LocalDate startDate,
            LocalDate endDate,
            boolean sequentialRelease,
            boolean isPublic,
            CourseStatus status
    ) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sequentialRelease = sequentialRelease;
        this.isPublic = isPublic;
        this.status = status;
    }
}
