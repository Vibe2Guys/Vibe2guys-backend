package com.vibe2guys.backend.ai.domain;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ai_follow_up_questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiFollowUpQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(name = "context_type", nullable = false, length = 30)
    private FollowUpContextType contextType;

    @Column(name = "source_text", nullable = false, length = 10000)
    private String sourceText;

    @Column(name = "question_text", nullable = false, length = 2000)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 20)
    private FollowUpDifficultyLevel difficultyLevel;

    @Builder
    private AiFollowUpQuestion(
            Course course,
            Content content,
            User student,
            FollowUpContextType contextType,
            String sourceText,
            String questionText,
            FollowUpDifficultyLevel difficultyLevel
    ) {
        this.course = course;
        this.content = content;
        this.student = student;
        this.contextType = contextType;
        this.sourceText = sourceText;
        this.questionText = questionText;
        this.difficultyLevel = difficultyLevel;
    }
}
