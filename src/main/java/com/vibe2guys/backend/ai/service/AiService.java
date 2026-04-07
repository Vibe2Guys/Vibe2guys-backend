package com.vibe2guys.backend.ai.service;

import com.vibe2guys.backend.ai.domain.AiFollowUpAnalysis;
import com.vibe2guys.backend.ai.domain.AiFollowUpQuestion;
import com.vibe2guys.backend.ai.domain.AiFollowUpResponse;
import com.vibe2guys.backend.ai.domain.FollowUpContextType;
import com.vibe2guys.backend.ai.domain.FollowUpDifficultyLevel;
import com.vibe2guys.backend.ai.dto.CreateFollowUpQuestionRequest;
import com.vibe2guys.backend.ai.dto.CreateFollowUpResponseRequest;
import com.vibe2guys.backend.ai.dto.FollowUpAnalysisResponse;
import com.vibe2guys.backend.ai.dto.FollowUpAnswerResponse;
import com.vibe2guys.backend.ai.dto.FollowUpQuestionResponse;
import com.vibe2guys.backend.ai.repository.AiFollowUpAnalysisRepository;
import com.vibe2guys.backend.ai.repository.AiFollowUpQuestionRepository;
import com.vibe2guys.backend.ai.repository.AiFollowUpResponseRepository;
import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.course.domain.Content;
import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.course.domain.CourseInstructor;
import com.vibe2guys.backend.course.domain.EnrollmentStatus;
import com.vibe2guys.backend.course.repository.ContentRepository;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.course.repository.CourseInstructorRepository;
import com.vibe2guys.backend.course.repository.CourseRepository;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiService {

    private final AiFollowUpQuestionRepository questionRepository;
    private final AiFollowUpResponseRepository responseRepository;
    private final AiFollowUpAnalysisRepository analysisRepository;
    private final CourseRepository courseRepository;
    private final ContentRepository contentRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final UserService userService;

    @Transactional
    public FollowUpQuestionResponse createFollowUpQuestion(Long requesterId, CreateFollowUpQuestionRequest request) {
        User requester = userService.getById(requesterId);
        User student = userService.getById(request.studentId());
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "강의를 찾을 수 없습니다."));
        Content content = request.contentId() == null ? null : contentRepository.findById(request.contentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "콘텐츠를 찾을 수 없습니다."));

        validateQuestionCreateAccess(requester, student, course, content);

        String normalizedSource = request.sourceText().trim();
        FollowUpDifficultyLevel difficultyLevel = inferDifficulty(normalizedSource);
        String questionText = generateQuestion(request.contextType(), normalizedSource, difficultyLevel);

        AiFollowUpQuestion question = questionRepository.save(AiFollowUpQuestion.builder()
                .course(course)
                .content(content)
                .student(student)
                .contextType(request.contextType())
                .sourceText(normalizedSource)
                .questionText(questionText)
                .difficultyLevel(difficultyLevel)
                .build());
        return FollowUpQuestionResponse.from(question);
    }

    @Transactional
    public FollowUpAnswerResponse createFollowUpResponse(Long questionId, Long requesterId, CreateFollowUpResponseRequest request) {
        User student = userService.getById(requesterId);
        if (student.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "학생만 꼬리질문 답변을 제출할 수 있습니다.");
        }

        AiFollowUpQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "꼬리질문을 찾을 수 없습니다."));
        if (!question.getStudent().getId().equals(student.getId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "본인에게 생성된 꼬리질문만 답변할 수 있습니다.");
        }

        OffsetDateTime submittedAt = OffsetDateTime.now();
        int delaySeconds = (int) Math.max(0, Duration.between(question.getCreatedAt(), submittedAt).toSeconds());
        AiFollowUpResponse response = responseRepository.save(AiFollowUpResponse.builder()
                .question(question)
                .student(student)
                .answerText(request.answerText().trim())
                .responseDelaySeconds(delaySeconds)
                .submittedAt(submittedAt)
                .build());

        analysisRepository.save(AiFollowUpAnalysis.builder()
                .question(question)
                .response(response)
                .understandingScore(calculateUnderstandingScore(question.getSourceText(), response.getAnswerText(), delaySeconds))
                .feedback(buildFeedback(question.getSourceText(), response.getAnswerText(), delaySeconds))
                .analyzedAt(submittedAt)
                .build());
        return FollowUpAnswerResponse.from(response);
    }

    public FollowUpAnalysisResponse getFollowUpAnalysis(Long questionId, Long requesterId) {
        User requester = userService.getById(requesterId);
        AiFollowUpQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "꼬리질문을 찾을 수 없습니다."));
        validateAnalysisAccess(requester, question);

        AiFollowUpAnalysis analysis = analysisRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTENT_NOT_FOUND, "분석 결과를 찾을 수 없습니다."));
        return FollowUpAnalysisResponse.of(analysis);
    }

    private void validateQuestionCreateAccess(User requester, User student, Course course, Content content) {
        if (content != null && !content.getCourse().getId().equals(course.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "contentId는 요청한 강의에 속해야 합니다.");
        }

        if (requester.getRole() == UserRole.ADMIN) {
            return;
        }
        if (requester.getRole() == UserRole.INSTRUCTOR) {
            CourseInstructor instructor = courseInstructorRepository.findByInstructorId(requester.getId()).stream()
                    .filter(item -> item.getCourse().getId().equals(course.getId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의에 대해서만 꼬리질문을 생성할 수 있습니다."));
            instructor.getCourse();
            ensureStudentEnrolled(course.getId(), student.getId());
            return;
        }
        if (requester.getRole() == UserRole.STUDENT && requester.getId().equals(student.getId())) {
            ensureStudentEnrolled(course.getId(), student.getId());
            return;
        }
        throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "꼬리질문 생성 권한이 없습니다.");
    }

    private void validateAnalysisAccess(User requester, AiFollowUpQuestion question) {
        if (requester.getRole() == UserRole.ADMIN) {
            return;
        }
        if (requester.getRole() == UserRole.STUDENT && requester.getId().equals(question.getStudent().getId())) {
            return;
        }
        if (requester.getRole() == UserRole.INSTRUCTOR) {
            courseInstructorRepository.findByInstructorId(requester.getId()).stream()
                    .filter(item -> item.getCourse().getId().equals(question.getCourse().getId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의의 꼬리질문 분석만 조회할 수 있습니다."));
            return;
        }
        throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "분석 조회 권한이 없습니다.");
    }

    private void ensureStudentEnrolled(Long courseId, Long studentId) {
        courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, studentId)
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 학생에 대해서만 생성할 수 있습니다."));
    }

    private FollowUpDifficultyLevel inferDifficulty(String sourceText) {
        int length = sourceText.length();
        if (length >= 300) {
            return FollowUpDifficultyLevel.HARD;
        }
        if (length >= 120) {
            return FollowUpDifficultyLevel.MEDIUM;
        }
        return FollowUpDifficultyLevel.EASY;
    }

    private String generateQuestion(FollowUpContextType contextType, String sourceText, FollowUpDifficultyLevel difficultyLevel) {
        String seed = extractSeed(sourceText);
        return switch (contextType) {
            case CONTENT -> "방금 학습한 \"" + seed + "\" 개념을 실제 사례에 적용하면 어떤 결과가 나올까요?";
            case QUIZ -> difficultyLevel == FollowUpDifficultyLevel.HARD
                    ? "\"" + seed + "\" 답변을 더 일반화하면 어떤 조건에서도 성립할 수 있을까요?"
                    : "\"" + seed + "\"와 관련해 왜 그런 선택을 했는지 설명해볼 수 있나요?";
            case ASSIGNMENT -> "작성한 내용 중 \"" + seed + "\" 부분을 이전 개념과 연결해서 다시 설명해볼 수 있나요?";
        };
    }

    private String extractSeed(String sourceText) {
        String normalized = sourceText.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 30) {
            return normalized;
        }
        return normalized.substring(0, 30).trim();
    }

    private int calculateUnderstandingScore(String sourceText, String answerText, int delaySeconds) {
        int overlapScore = countOverlap(sourceText, answerText) * 10;
        int lengthScore = Math.min(40, answerText.length() / 20);
        int delayPenalty = delaySeconds > 600 ? 20 : delaySeconds > 180 ? 10 : 0;
        return Math.max(0, Math.min(100, overlapScore + lengthScore + 30 - delayPenalty));
    }

    private int countOverlap(String sourceText, String answerText) {
        List<String> tokens = List.of(sourceText.toLowerCase().split("\\s+"));
        String answerLower = answerText.toLowerCase();
        int count = 0;
        for (String token : tokens) {
            if (token.length() >= 3 && answerLower.contains(token)) {
                count++;
            }
        }
        return Math.min(count, 5);
    }

    private String buildFeedback(String sourceText, String answerText, int delaySeconds) {
        if (answerText.length() < 30) {
            return "답변이 짧아 개념 연결이 충분히 드러나지 않았습니다. 근거를 한두 문장 더 덧붙여보세요.";
        }
        if (delaySeconds > 600) {
            return "답변 내용은 나쁘지 않지만 응답 시간이 길었습니다. 핵심 개념을 먼저 정리한 뒤 답해보세요.";
        }
        if (countOverlap(sourceText, answerText) < 2) {
            return "원문과의 연결이 약합니다. 핵심 용어를 활용해 개념 관계를 더 분명히 설명해보세요.";
        }
        return "핵심 개념을 잘 연결했습니다. 다음에는 실제 사례까지 함께 설명해보면 더 좋습니다.";
    }
}
