package com.vibe2guys.backend.quiz.domain;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "quiz_submission_answers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizSubmissionAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_submission_id", nullable = false)
    private QuizSubmission quizSubmission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(name = "selected_choice", length = 2000)
    private String selectedChoice;

    @Column(name = "answer_text", length = 10000)
    private String answerText;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "awarded_score", nullable = false)
    private int awardedScore;

    @Builder
    private QuizSubmissionAnswer(
            QuizSubmission quizSubmission,
            QuizQuestion question,
            String selectedChoice,
            String answerText,
            Boolean correct,
            int awardedScore
    ) {
        this.quizSubmission = quizSubmission;
        this.question = question;
        this.selectedChoice = selectedChoice;
        this.answerText = answerText;
        this.correct = correct;
        this.awardedScore = awardedScore;
    }
}
