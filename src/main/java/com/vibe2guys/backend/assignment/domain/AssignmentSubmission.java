package com.vibe2guys.backend.assignment.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "assignment_submissions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentSubmission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @Column(name = "answer_text", length = 10000)
    private String answerText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssignmentSubmissionStatus status;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @Builder
    private AssignmentSubmission(
            Assignment assignment,
            Course course,
            User student,
            String answerText,
            AssignmentSubmissionStatus status,
            OffsetDateTime submittedAt
    ) {
        this.assignment = assignment;
        this.course = course;
        this.student = student;
        this.answerText = answerText;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    public void resubmit(String answerText, AssignmentSubmissionStatus status, OffsetDateTime submittedAt) {
        this.answerText = answerText;
        this.status = status;
        this.submittedAt = submittedAt;
    }
}
