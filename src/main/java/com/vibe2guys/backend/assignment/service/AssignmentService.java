package com.vibe2guys.backend.assignment.service;

import com.vibe2guys.backend.assignment.domain.Assignment;
import com.vibe2guys.backend.assignment.domain.AssignmentSubmission;
import com.vibe2guys.backend.assignment.domain.AssignmentSubmissionFile;
import com.vibe2guys.backend.assignment.domain.AssignmentSubmissionStatus;
import com.vibe2guys.backend.assignment.dto.AssignmentDetailResponse;
import com.vibe2guys.backend.assignment.dto.AssignmentListItemResponse;
import com.vibe2guys.backend.assignment.dto.AssignmentSubmissionResponse;
import com.vibe2guys.backend.assignment.dto.AssignmentSubmissionSummaryResponse;
import com.vibe2guys.backend.assignment.dto.CreateAssignmentSubmissionRequest;
import com.vibe2guys.backend.assignment.repository.AssignmentRepository;
import com.vibe2guys.backend.assignment.repository.AssignmentSubmissionFileRepository;
import com.vibe2guys.backend.assignment.repository.AssignmentSubmissionRepository;
import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.course.domain.CourseEnrollment;
import com.vibe2guys.backend.course.domain.CourseInstructor;
import com.vibe2guys.backend.course.domain.EnrollmentStatus;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.course.repository.CourseInstructorRepository;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final AssignmentSubmissionFileRepository assignmentSubmissionFileRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final UserService userService;

    public List<AssignmentListItemResponse> getAssignments(Long courseId, Long userId) {
        User user = userService.getById(userId);
        validateCourseAccess(courseId, user);

        List<Assignment> assignments = assignmentRepository.findByCourseIdOrderByDueAtAsc(courseId);
        List<AssignmentListItemResponse> responses = new ArrayList<>();
        for (Assignment assignment : assignments) {
            boolean submitted = assignmentSubmissionRepository.findByAssignmentIdAndStudentId(assignment.getId(), userId).isPresent();
            responses.add(AssignmentListItemResponse.of(assignment, submitted));
        }
        return responses;
    }

    public AssignmentDetailResponse getAssignmentDetail(Long assignmentId, Long userId) {
        User user = userService.getById(userId);
        Assignment assignment = getAccessibleAssignment(assignmentId, user);
        AssignmentSubmissionSummaryResponse mySubmission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, userId)
                .map(AssignmentSubmissionSummaryResponse::from)
                .orElse(AssignmentSubmissionSummaryResponse.notSubmitted());
        return AssignmentDetailResponse.of(assignment, mySubmission);
    }

    @Transactional
    public AssignmentSubmissionResponse submitAssignment(Long assignmentId, Long userId, CreateAssignmentSubmissionRequest request) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "학생만 과제를 제출할 수 있습니다.");
        }

        Assignment assignment = getAccessibleAssignment(assignmentId, user);
        if (assignment.isTeamAssignment()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "팀 과제 제출은 아직 지원하지 않습니다.");
        }
        if (assignmentSubmissionRepository.findByAssignmentIdAndStudentId(assignmentId, userId).isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "이미 제출한 과제입니다. 재제출 API를 사용하세요.");
        }

        OffsetDateTime submittedAt = OffsetDateTime.now();
        AssignmentSubmissionStatus status = submittedAt.isAfter(assignment.getDueAt())
                ? AssignmentSubmissionStatus.LATE
                : AssignmentSubmissionStatus.SUBMITTED;

        AssignmentSubmission submission = assignmentSubmissionRepository.save(AssignmentSubmission.builder()
                .assignment(assignment)
                .course(assignment.getCourse())
                .student(user)
                .answerText(request.answerText())
                .status(status)
                .submittedAt(submittedAt)
                .build());

        List<String> fileUrls = request.fileUrls() == null ? Collections.emptyList() : request.fileUrls();
        for (String fileUrl : fileUrls) {
            assignmentSubmissionFileRepository.save(AssignmentSubmissionFile.builder()
                    .submission(submission)
                    .fileUrl(fileUrl)
                    .build());
        }

        return AssignmentSubmissionResponse.from(submission);
    }

    private Assignment getAccessibleAssignment(Long assignmentId, User user) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND, "과제를 찾을 수 없습니다."));
        validateCourseAccess(assignment.getCourse().getId(), user);
        return assignment;
    }

    private void validateCourseAccess(Long courseId, User user) {
        if (user.getRole() == UserRole.INSTRUCTOR) {
            CourseInstructor instructor = courseInstructorRepository.findByInstructorId(user.getId()).stream()
                    .filter(item -> item.getCourse().getId().equals(courseId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의만 접근할 수 있습니다."));
            if (instructor.getCourse() == null) {
                throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "담당 강의만 접근할 수 있습니다.");
            }
            return;
        }

        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "수강 중인 강의만 접근할 수 있습니다."));
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "활성 수강 상태가 아닙니다.");
        }
    }
}
