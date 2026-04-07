package com.vibe2guys.backend.quiz.domain;

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
@Table(name = "quiz_submissions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizSubmission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @Column(name = "objective_score", nullable = false)
    private int objectiveScore;

    @Column(name = "subjective_score")
    private Integer subjectiveScore;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuizSubmissionStatus status;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @Builder
    private QuizSubmission(
            Quiz quiz,
            Course course,
            User student,
            int objectiveScore,
            Integer subjectiveScore,
            int totalScore,
            QuizSubmissionStatus status,
            OffsetDateTime submittedAt
    ) {
        this.quiz = quiz;
        this.course = course;
        this.student = student;
        this.objectiveScore = objectiveScore;
        this.subjectiveScore = subjectiveScore;
        this.totalScore = totalScore;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    public void updateScores(int objectiveScore, Integer subjectiveScore, int totalScore) {
        this.objectiveScore = objectiveScore;
        this.subjectiveScore = subjectiveScore;
        this.totalScore = totalScore;
    }
}
