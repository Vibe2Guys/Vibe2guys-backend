package com.vibe2guys.backend.quiz.domain;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Entity
@Table(name = "quiz_questions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuizQuestionType questionType;

    @Column(name = "question_text", nullable = false, length = 2000)
    private String questionText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "choices_json", columnDefinition = "jsonb")
    private List<String> choicesJson;

    @Column(name = "answer_key", length = 2000)
    private String answerKey;

    @Column(nullable = false)
    private int score;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    private QuizQuestion(
            Quiz quiz,
            QuizQuestionType questionType,
            String questionText,
            List<String> choicesJson,
            String answerKey,
            int score,
            int sortOrder
    ) {
        this.quiz = quiz;
        this.questionType = questionType;
        this.questionText = questionText;
        this.choicesJson = choicesJson;
        this.answerKey = answerKey;
        this.score = score;
        this.sortOrder = sortOrder;
    }
}
