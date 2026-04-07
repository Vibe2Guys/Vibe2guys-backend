package com.vibe2guys.backend.quiz.dto;

import com.vibe2guys.backend.quiz.domain.QuizSubmission;

public record QuizResultResponse(
        Long quizId,
        int totalScore,
        int objectiveScore,
        Integer subjectiveScore,
        String feedback
) {
    public static QuizResultResponse from(QuizSubmission submission) {
        return new QuizResultResponse(
                submission.getQuiz().getId(),
                submission.getTotalScore(),
                submission.getObjectiveScore(),
                submission.getSubjectiveScore(),
                submission.getSubjectiveScore() == null ? "서술형 평가는 아직 진행 중입니다." : "퀴즈 결과 조회 성공"
        );
    }
}
