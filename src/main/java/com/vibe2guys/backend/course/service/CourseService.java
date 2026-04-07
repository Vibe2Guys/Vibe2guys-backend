package com.vibe2guys.backend.course.service;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.course.domain.CourseEnrollment;
import com.vibe2guys.backend.course.domain.CourseInstructor;
import com.vibe2guys.backend.course.domain.EnrollmentStatus;
import com.vibe2guys.backend.course.dto.EnrollmentResponse;
import com.vibe2guys.backend.course.dto.MyCourseItemResponse;
import com.vibe2guys.backend.course.repository.CourseEnrollmentRepository;
import com.vibe2guys.backend.course.repository.CourseInstructorRepository;
import com.vibe2guys.backend.course.repository.CourseRepository;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final UserService userService;

    public List<MyCourseItemResponse> getMyCourses(Long userId) {
        User user = userService.getById(userId);
        if (user.getRole() == UserRole.INSTRUCTOR) {
            return mapInstructorCourses(courseInstructorRepository.findByInstructorId(userId));
        }
        return mapStudentCourses(courseEnrollmentRepository.findByStudentId(userId));
    }

    @Transactional
    public EnrollmentResponse enroll(Long courseId, Long userId) {
        User user = userService.getById(userId);
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "학생만 수강 신청할 수 있습니다.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "강의를 찾을 수 없습니다."));

        if (courseEnrollmentRepository.findByCourseIdAndStudentId(courseId, userId).isPresent()) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_ENROLLED, "이미 수강 신청된 강의입니다.");
        }

        CourseEnrollment enrollment = courseEnrollmentRepository.save(CourseEnrollment.builder()
                .course(course)
                .student(user)
                .status(EnrollmentStatus.ENROLLED)
                .enrolledAt(OffsetDateTime.now())
                .build());

        return new EnrollmentResponse(enrollment.getCourse().getId(), enrollment.getStudent().getId(), enrollment.getStatus().name());
    }

    private List<MyCourseItemResponse> mapStudentCourses(List<CourseEnrollment> enrollments) {
        List<MyCourseItemResponse> responses = new ArrayList<>();
        for (CourseEnrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            responses.add(new MyCourseItemResponse(
                    course.getId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getThumbnailUrl(),
                    null,
                    0,
                    0,
                    0
            ));
        }
        return responses;
    }

    private List<MyCourseItemResponse> mapInstructorCourses(List<CourseInstructor> instructors) {
        List<MyCourseItemResponse> responses = new ArrayList<>();
        for (CourseInstructor instructor : instructors) {
            Course course = instructor.getCourse();
            responses.add(new MyCourseItemResponse(
                    course.getId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getThumbnailUrl(),
                    instructor.getInstructor().getName(),
                    0,
                    0,
                    0
            ));
        }
        return responses;
    }
}
