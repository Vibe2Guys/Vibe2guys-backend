package com.vibe2guys.backend.learning.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import com.vibe2guys.backend.course.domain.Content;
import com.vibe2guys.backend.course.domain.Course;
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

import java.time.Duration;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(
        name = "attendance_summaries",
        uniqueConstraints = @UniqueConstraint(name = "uk_attendance_content_student", columnNames = {"content_id", "student_user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceSummary extends BaseTimeEntity {

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

    @Column(name = "first_entered_at", nullable = false)
    private OffsetDateTime firstEnteredAt;

    @Column(name = "last_left_at")
    private OffsetDateTime lastLeftAt;

    @Column(name = "attendance_minutes", nullable = false)
    private int attendanceMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Builder
    private AttendanceSummary(
            Course course,
            Content content,
            User student,
            OffsetDateTime firstEnteredAt,
            OffsetDateTime lastLeftAt,
            int attendanceMinutes,
            AttendanceStatus status
    ) {
        this.course = course;
        this.content = content;
        this.student = student;
        this.firstEnteredAt = firstEnteredAt;
        this.lastLeftAt = lastLeftAt;
        this.attendanceMinutes = attendanceMinutes;
        this.status = status;
    }

    public void start(OffsetDateTime enteredAt) {
        if (this.firstEnteredAt == null) {
            this.firstEnteredAt = enteredAt;
        }
        this.status = AttendanceStatus.PRESENT;
    }

    public void finish(OffsetDateTime leftAt) {
        this.lastLeftAt = leftAt;
        if (firstEnteredAt != null && !leftAt.isBefore(firstEnteredAt)) {
            this.attendanceMinutes = (int) Duration.between(firstEnteredAt, leftAt).toMinutes();
        }
        this.status = AttendanceStatus.PRESENT;
    }
}
