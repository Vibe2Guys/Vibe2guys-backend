package com.vibe2guys.backend.assignment.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Entity
@Table(name = "assignment_submission_files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentSubmissionFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private AssignmentSubmission submission;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Builder
    private AssignmentSubmissionFile(AssignmentSubmission submission, String fileUrl) {
        this.submission = submission;
        this.fileUrl = fileUrl;
    }
}
