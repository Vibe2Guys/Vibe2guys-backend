package com.vibe2guys.backend.course.repository;

import com.vibe2guys.backend.course.domain.CourseAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseAnnouncementRepository extends JpaRepository<CourseAnnouncement, Long> {
    List<CourseAnnouncement> findByCourseIdOrderByPinnedDescCreatedAtDesc(Long courseId);
}
