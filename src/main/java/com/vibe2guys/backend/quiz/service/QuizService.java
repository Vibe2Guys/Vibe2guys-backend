package com.vibe2guys.backend.quiz.service;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.course.domain.CourseEnrollment;
import com.vibe2guys.backend.course.domain.CourseInstructor;
import com.vibe2guys.backend.course.domain.EnrollmentStatus;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.course.repository.CourseInstructorRepository;
import com.vibe2guys.backend.course.repository.CourseRepository;
import com.vibe2guys.backend.quiz.dto.CreateQuizQuestionRequest;
import com.vibe2guys.backend.quiz.dto.CreateQuizRequest;
import com.vibe2guys.backend.quiz.dto.CreateQuizResponse;
import com.vibe2guys.backend.quiz.domain.Quiz;
import com.vibe2guys.backend.quiz.domain.QuizQuestion;
import com.vibe2guys.backend.quiz.domain.QuizQuestionType;
import com.vibe2guys.backend.quiz.domain.QuizSubmission;
import com.vibe2guys.backend.quiz.domain.QuizSubmissionAnswer;
import com.vibe2guys.backend.quiz.domain.QuizSubmissionStatus;
import com.vibe2guys.backend.quiz.dto.CreateQuizSubmissionRequest;
import com.vibe2guys.backend.quiz.dto.QuizDetailResponse;
import com.vibe2guys.backend.quiz.dto.QuizListItemResponse;
import com.vibe2guys.backend.quiz.dto.QuizQuestionResponse;
import com.vibe2guys.backend.quiz.dto.QuizResultResponse;
import com.vibe2guys.backend.quiz.dto.QuizSubmissionAnswerRequest;
import com.vibe2guys.backend.quiz.dto.QuizSubmissionResponse;
import com.vibe2guys.backend.quiz.repository.QuizQuestionRepository;
import com.vibe2guys.backend.quiz.repository.QuizRepository;
import com.vibe2guys.backend.quiz.repository.QuizSubmissionAnswerRepository;
import com.vibe2guys.backend.quiz.repository.QuizSubmissionRepository;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizSubmissionAnswerRepository quizSubmissionAnswerRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;

    @Transactional
    public CreateQuizResponse createQuiz(Long courseId, Long userId, CreateQuizRequest request) {
        User user = userService.getById(userId);
        Course course = getManageableCourse(courseId, user);
        validateCreateQuizRequest(request);

        Quiz quiz = quizRepository.save(Quiz.builder()
                .course(course)
                .title(request.title().trim())
                .dueAt(request.dueAt())
                .createdBy(user)
                .build());

        for (CreateQuizQuestionRequest questionRequest : request.questions()) {
            quizQuestionRepository.save(QuizQuestion.builder()
                    .quiz(quiz)
                    .questionType(questionRequest.questionType())
                    .questionText(questionRequest.questionText().trim())
                    .choicesJson(questionRequest.choices())
                    .answerKey(questionRequest.answerKey() != null ? questionRequest.answerKey().trim() : null)
                    .score(questionRequest.score())
                    .sortOrder(questionRequest.sortOrder())
                    .build());
        }

        return CreateQuizResponse.of(quiz, request.questions().size());
    }

    public List<QuizListItemResponse> getQuizzes(Long courseId, Long userId) {
        User user = userService.getById(userId);
        validateCourseAccess(courseId, user);

        List<Quiz> quizzes = quizRepository.findByCourseIdOrderByDueAtAsc(courseId);
        List<QuizListItemResponse> responses = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            boolean submitted = quizSubmissionRepository.findByQuizIdAndStudentId(quiz.getId(), userId).isPresent();
            responses.add(QuizListItemResponse.of(quiz, submitted));
        }
        return responses;
    }

    public QuizDetailResponse getQuizDetail(Long quizId, Long userId) {
        User user = userService.getById(userId);
        Quiz quiz = getAccessibleQuiz(quizId, user);
        List<QuizQuestionResponse> questions = quizQuestionRepository.findByQuizIdOrderBySortOrderAsc(quizId).stream()
                .map(QuizQuestionResponse::from)
                .toList();
        return QuizDetailResponse.of(quiz, questions);
    }

    @Transactional
    public QuizSubmissionResponse submitQuiz(Long quizId, Long userId, CreateQuizSubmissionRequest request) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "학생만 퀴즈를 제출할 수 있습니다.");
        }

        Quiz quiz = getAccessibleQuiz(quizId, user);
        if (quizSubmissionRepository.findByQuizIdAndStudentId(quizId, userId).isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "이미 제출한 퀴즈입니다.");
        }

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderBySortOrderAsc(quizId);
        Map<Long, QuizQuestion> questionMap = questions.stream().collect(Collectors.toMap(QuizQuestion::getId, Function.identity()));
        validateAnswerSet(request.answers(), questionMap);

        int objectiveScore = 0;
        Integer subjectiveScore = null;
        OffsetDateTime submittedAt = OffsetDateTime.now();
        QuizSubmissionStatus status = submittedAt.isAfter(quiz.getDueAt()) ? QuizSubmissionStatus.LATE : QuizSubmissionStatus.SUBMITTED;

        QuizSubmission submission = quizSubmissionRepository.save(QuizSubmission.builder()
                .quiz(quiz)
                .course(quiz.getCourse())
                .student(user)
                .objectiveScore(0)
                .subjectiveScore(subjectiveScore)
                .totalScore(0)
                .status(status)
                .submittedAt(submittedAt)
                .build());

        boolean hasSubjective = false;
        for (QuizSubmissionAnswerRequest answerRequest : request.answers()) {
            QuizQuestion question = questionMap.get(answerRequest.questionId());
            int awardedScore = 0;
            Boolean correct = null;

            if (question.getQuestionType() == QuizQuestionType.MULTIPLE_CHOICE) {
                correct = question.getAnswerKey() != null && question.getAnswerKey().equals(answerRequest.selectedChoice());
                awardedScore = Boolean.TRUE.equals(correct) ? question.getScore() : 0;
                objectiveScore += awardedScore;
            } else {
                hasSubjective = true;
            }

            quizSubmissionAnswerRepository.save(QuizSubmissionAnswer.builder()
                    .quizSubmission(submission)
                    .question(question)
                    .selectedChoice(answerRequest.selectedChoice())
                    .answerText(answerRequest.answerText())
                    .correct(correct)
                    .awardedScore(awardedScore)
                    .build());
        }

        int totalScore = objectiveScore;
        Integer finalSubjectiveScore = hasSubjective ? null : 0;
        submission.updateScores(objectiveScore, finalSubjectiveScore, totalScore);

        return QuizSubmissionResponse.from(submission);
    }

    public QuizResultResponse getMyResult(Long quizId, Long userId) {
        User user = userService.getById(userId);
        getAccessibleQuiz(quizId, user);
        QuizSubmission submission = quizSubmissionRepository.findByQuizIdAndStudentId(quizId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_SUBMISSION_NOT_FOUND, "제출한 퀴즈 결과가 없습니다."));
        return QuizResultResponse.from(submission);
    }

    private Quiz getAccessibleQuiz(Long quizId, User user) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND, "퀴즈를 찾을 수 없습니다."));
        validateCourseAccess(quiz.getCourse().getId(), user);
        return quiz;
    }

    private void validateCourseAccess(Long courseId, User user) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }
        if (user.getRole() == UserRole.INSTRUCTOR) {
            courseInstructorRepository.findByInstructorId(user.getId()).stream()
                    .filter(item -> item.getCourse().getId().equals(courseId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의만 접근할 수 있습니다."));
            return;
        }

        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 강의만 접근할 수 있습니다."));
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "활성 수강 상태가 아닙니다.");
        }
    }

    private Course getManageableCourse(Long courseId, User user) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "강의를 찾을 수 없습니다."));
        if (user.getRole() == UserRole.ADMIN) {
            return course;
        }
        if (user.getRole() != UserRole.INSTRUCTOR) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "강의 관리 권한이 없습니다.");
        }
        courseInstructorRepository.findByInstructorId(user.getId()).stream()
                .filter(item -> item.getCourse().getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의만 관리할 수 있습니다."));
        return course;
    }

    private void validateCreateQuizRequest(CreateQuizRequest request) {
        if (request.dueAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "마감 기한은 현재 시각 이후여야 합니다.");
        }
        for (CreateQuizQuestionRequest question : request.questions()) {
            validateQuestion(question);
        }
    }

    private void validateQuestion(CreateQuizQuestionRequest question) {
        if (question.questionType() == QuizQuestionType.MULTIPLE_CHOICE) {
            if (question.choices() == null || question.choices().size() < 2) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "객관식 문제는 2개 이상의 선택지가 필요합니다.");
            }
            if (question.answerKey() == null || question.answerKey().isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "객관식 문제는 answerKey가 필요합니다.");
            }
            boolean answerExists = question.choices().stream()
                    .filter(choice -> choice != null)
                    .map(String::trim)
                    .anyMatch(choice -> choice.equals(question.answerKey().trim()));
            if (!answerExists) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "객관식 answerKey는 선택지 중 하나여야 합니다.");
            }
            return;
        }

        if (question.choices() != null && !question.choices().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "주관식 문제에는 choices를 포함할 수 없습니다.");
        }
    }

    private void validateAnswerSet(List<QuizSubmissionAnswerRequest> answers, Map<Long, QuizQuestion> questionMap) {
        if (answers.size() != questionMap.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "모든 문제에 대한 답안을 제출해야 합니다.");
        }
        for (QuizSubmissionAnswerRequest answer : answers) {
            QuizQuestion question = questionMap.get(answer.questionId());
            if (question == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "해당 퀴즈에 없는 문제입니다.");
            }
            if (question.getQuestionType() == QuizQuestionType.MULTIPLE_CHOICE && (answer.selectedChoice() == null || answer.selectedChoice().isBlank())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "객관식 답안은 필수입니다.");
            }
            if (question.getQuestionType() == QuizQuestionType.SUBJECTIVE && (answer.answerText() == null || answer.answerText().isBlank())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "주관식 답안은 필수입니다.");
            }
        }
    }
}
