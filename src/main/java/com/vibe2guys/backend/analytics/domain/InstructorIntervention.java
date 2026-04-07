package com.vibe2guys.backend.analytics.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Entity
@Table(name = "instructor_interventions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InstructorIntervention extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_user_id", nullable = false)
    private User instructor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InterventionType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resource_urls_json", columnDefinition = "jsonb")
    private List<String> resourceUrls;

    @Builder
    private InstructorIntervention(
            Course course,
            User student,
            User instructor,
            InterventionType type,
            String title,
            String message,
            List<String> resourceUrls
    ) {
        this.course = course;
        this.student = student;
        this.instructor = instructor;
        this.type = type;
        this.title = title;
        this.message = message;
        this.resourceUrls = resourceUrls;
    }
}
