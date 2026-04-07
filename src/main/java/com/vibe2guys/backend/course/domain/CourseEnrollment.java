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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(
        name = "course_enrollments",
        uniqueConstraints = @UniqueConstraint(name = "uk_course_enrollments_course_student", columnNames = {"course_id", "student_user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseEnrollment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @Column(name = "enrolled_at", nullable = false)
    private OffsetDateTime enrolledAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Builder
    private CourseEnrollment(Course course, User student, EnrollmentStatus status, OffsetDateTime enrolledAt, OffsetDateTime completedAt) {
        this.course = course;
        this.student = student;
        this.status = status;
        this.enrolledAt = enrolledAt;
        this.completedAt = completedAt;
    }
}
