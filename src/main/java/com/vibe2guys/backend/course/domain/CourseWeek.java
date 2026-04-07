package com.vibe2guys.backend.course.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
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

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(
        name = "course_weeks",
        uniqueConstraints = @UniqueConstraint(name = "uk_course_weeks_course_week_number", columnNames = {"course_id", "week_number"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseWeek extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "open_at")
    private OffsetDateTime openAt;

    @Builder
    private CourseWeek(Course course, int weekNumber, String title, OffsetDateTime openAt) {
        this.course = course;
        this.weekNumber = weekNumber;
        this.title = title;
        this.openAt = openAt;
    }
}
