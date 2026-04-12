package com.vibe2guys.backend.course.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import com.vibe2guys.backend.user.domain.User;
import jakarta.persistence.Column;
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
        name = "course_student_memos",
        uniqueConstraints = @UniqueConstraint(name = "uk_course_student_memo", columnNames = {"course_id", "student_user_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseStudentMemo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @Column(name = "memo_text", length = 2000)
    private String memoText;

    @Builder
    private CourseStudentMemo(Course course, User student, String memoText) {
        this.course = course;
        this.student = student;
        this.memoText = memoText;
    }

    public void updateMemoText(String memoText) {
        this.memoText = memoText;
    }
}
