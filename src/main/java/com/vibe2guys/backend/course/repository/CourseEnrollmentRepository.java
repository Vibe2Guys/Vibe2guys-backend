package com.vibe2guys.backend.course.repository;

import com.vibe2guys.backend.course.domain.CourseEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    Optional<CourseEnrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);

    List<CourseEnrollment> findByStudentId(Long studentId);

    List<CourseEnrollment> findByStudentIdAndStatus(Long studentId, com.vibe2guys.backend.course.domain.EnrollmentStatus status);

    List<CourseEnrollment> findByStudentIdAndCourseIdInAndStatus(
            Long studentId,
            List<Long> courseIds,
            com.vibe2guys.backend.course.domain.EnrollmentStatus status
    );

    List<CourseEnrollment> findByCourseIdAndStatus(Long courseId, com.vibe2guys.backend.course.domain.EnrollmentStatus status);

    boolean existsByCourseIdAndStudentIdAndStatus(Long courseId, Long studentId, com.vibe2guys.backend.course.domain.EnrollmentStatus status);

    @Query("""
            select ce
            from CourseEnrollment ce
            join ce.student s
            where ce.course.id = :courseId
              and (:keyword = '' or lower(s.name) like lower(concat('%', :keyword, '%'))
                   or lower(s.email) like lower(concat('%', :keyword, '%')))
            """)
    Page<CourseEnrollment> findStudentPageByCourseId(Long courseId, String keyword, Pageable pageable);
}
