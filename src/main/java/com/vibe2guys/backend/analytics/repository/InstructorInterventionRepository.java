package com.vibe2guys.backend.analytics.repository;

import com.vibe2guys.backend.analytics.domain.InstructorIntervention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstructorInterventionRepository extends JpaRepository<InstructorIntervention, Long> {

    List<InstructorIntervention> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    List<InstructorIntervention> findByCourseIdAndStudentIdOrderByCreatedAtDesc(Long courseId, Long studentId);
}
