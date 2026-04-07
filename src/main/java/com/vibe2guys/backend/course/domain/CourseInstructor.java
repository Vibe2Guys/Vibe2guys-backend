package com.vibe2guys.backend.course.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import com.vibe2guys.backend.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "course_instructors",
        uniqueConstraints = @UniqueConstraint(name = "uk_course_instructors_course_user", columnNames = {"course_id", "instructor_user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseInstructor extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_user_id", nullable = false)
    private User instructor;

    @Builder
    private CourseInstructor(Course course, User instructor) {
        this.course = course;
        this.instructor = instructor;
    }
}
