package com.vibe2guys.backend.analytics.repository;

import com.vibe2guys.backend.analytics.domain.DailyAnalyticsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyAnalyticsSnapshotRepository extends JpaRepository<DailyAnalyticsSnapshot, Long> {
    Optional<DailyAnalyticsSnapshot> findByStudentIdAndCourseIdAndSnapshotDate(Long studentId, Long courseId, LocalDate snapshotDate);

    Optional<DailyAnalyticsSnapshot> findTopByStudentIdAndCourseIdOrderBySnapshotDateDesc(Long studentId, Long courseId);

    List<DailyAnalyticsSnapshot> findByStudentIdAndSnapshotDate(Long studentId, LocalDate snapshotDate);

    List<DailyAnalyticsSnapshot> findByCourseIdAndSnapshotDate(Long courseId, LocalDate snapshotDate);
}
