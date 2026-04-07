# DB Schema Draft

## Purpose

This document defines the MVP database schema draft for the Spring Boot backend.
The goal is to make entity boundaries, table responsibilities, and analytics storage explicit before implementation.

## Database Choice

- `PostgreSQL`
- primary key strategy: `bigint`
- timestamps should use timezone-aware values at the application boundary

## Design Principles

- transactional tables and event tables are separated logically
- learner activity is stored twice when needed: raw event log + read-optimized summary
- daily analytics are persisted for fast dashboard reads
- soft-delete is preferred over hard-delete for academic records

## Core Relationship Map

- one `user` can enroll in many `courses`
- one `course` has many `weeks`
- one `week` has many `contents`
- one `course` has many `assignments` and `quizzes`
- one `course` has many `teams`
- one `team` has many `team_members`
- one learner generates many `learning_events`
- one learner has many `daily_analytics_snapshots`

## 1. Users / Auth

### `users`

Purpose:

- platform identity

Columns:

- `id`
- `email` unique
- `password_hash`
- `name`
- `role` (`STUDENT`, `INSTRUCTOR`, `ADMIN`)
- `profile_image_url`
- `status` (`ACTIVE`, `INACTIVE`, `SUSPENDED`)
- `created_at`
- `updated_at`

Indexes:

- unique index on `email`
- index on `role`

### `refresh_tokens`

Purpose:

- refresh token rotation and revocation support

Columns:

- `id`
- `user_id`
- `token`
- `device_name`
- `ip_address`
- `expires_at`
- `revoked_at`
- `created_at`

Indexes:

- index on (`user_id`)
- unique index on `token`

## 2. Course Structure

### `courses`

Columns:

- `id`
- `title`
- `description`
- `thumbnail_url`
- `start_date`
- `end_date`
- `is_sequential_release`
- `status` (`DRAFT`, `PUBLISHED`, `ARCHIVED`)
- `created_by`
- `created_at`
- `updated_at`

### `course_instructors`

Purpose:

- support one or more instructors per course

Columns:

- `id`
- `course_id`
- `instructor_user_id`
- `created_at`

Unique:

- unique (`course_id`, `instructor_user_id`)

### `course_enrollments`

Columns:

- `id`
- `course_id`
- `student_user_id`
- `status` (`ENROLLED`, `DROPPED`, `COMPLETED`)
- `enrolled_at`
- `completed_at`

Unique:

- unique (`course_id`, `student_user_id`)

Indexes:

- index on (`course_id`, `status`)
- index on (`student_user_id`)

### `course_weeks`

Columns:

- `id`
- `course_id`
- `week_number`
- `title`
- `open_at`
- `created_at`
- `updated_at`

Unique:

- unique (`course_id`, `week_number`)

### `contents`

Purpose:

- VOD, LIVE, DOCUMENT are unified here

Columns:

- `id`
- `course_id`
- `week_id`
- `type` (`VOD`, `LIVE`, `DOCUMENT`)
- `title`
- `description`
- `video_url`
- `document_url`
- `duration_seconds`
- `scheduled_at`
- `open_at`
- `is_published`
- `created_at`
- `updated_at`

Indexes:

- index on (`course_id`, `week_id`)
- index on (`type`)

## 3. Attendance / Progress / Events

### `learning_events`

Purpose:

- append-only canonical event log
- single-table strategy for MVP

Columns:

- `id`
- `event_type`
- `actor_user_id`
- `course_id`
- `week_id`
- `content_id`
- `resource_type`
- `resource_id`
- `occurred_at`
- `payload_json` (`jsonb`)
- `schema_version`
- `created_at`

Recommended event types:

- `ATTENDANCE_ENTER`
- `ATTENDANCE_LEAVE`
- `CONTENT_PROGRESS`
- `PLAYBACK_PAUSE`
- `PLAYBACK_SEEK`
- `PLAYBACK_COMPLETE`
- `QUIZ_SUBMISSION`
- `ASSIGNMENT_SUBMISSION`
- `FOLLOW_UP_RESPONSE`
- `TEAM_CHAT_MESSAGE`
- `TEAM_TASK_ACTIVITY`
- `INTERVENTION_ACTION`

Indexes:

- index on (`actor_user_id`, `occurred_at`)
- index on (`course_id`, `event_type`, `occurred_at`)
- index on (`content_id`, `event_type`)

### `attendance_summaries`

Purpose:

- fast attendance read model per learner and live content

Columns:

- `id`
- `course_id`
- `content_id`
- `student_user_id`
- `first_entered_at`
- `last_left_at`
- `attendance_minutes`
- `status` (`PRESENT`, `LATE`, `ABSENT`)
- `updated_at`

Unique:

- unique (`content_id`, `student_user_id`)

### `content_progress_summaries`

Purpose:

- fast read model for VOD content consumption

Columns:

- `id`
- `course_id`
- `content_id`
- `student_user_id`
- `watched_seconds`
- `total_seconds`
- `progress_rate`
- `last_position_seconds`
- `replay_count`
- `is_completed`
- `last_event_type`
- `updated_at`

Unique:

- unique (`content_id`, `student_user_id`)

### `course_learning_summaries`

Purpose:

- course-level learner progress summary

Columns:

- `id`
- `course_id`
- `student_user_id`
- `attendance_rate`
- `progress_rate`
- `average_watch_time_minutes`
- `assignment_submit_rate`
- `quiz_average_score`
- `updated_at`

Unique:

- unique (`course_id`, `student_user_id`)

## 4. Assignments / Submissions

### `assignments`

Columns:

- `id`
- `course_id`
- `title`
- `description`
- `type` (`MULTIPLE_CHOICE`, `SUBJECTIVE`, `DESCRIPTIVE`)
- `team_assignment`
- `due_at`
- `created_by`
- `created_at`
- `updated_at`

### `assignment_submissions`

Columns:

- `id`
- `assignment_id`
- `course_id`
- `student_user_id`
- `team_id`
- `answer_text`
- `status` (`NOT_SUBMITTED`, `SUBMITTED`, `RESUBMITTED`, `LATE`)
- `submitted_at`
- `updated_at`

Indexes:

- index on (`assignment_id`, `student_user_id`)
- index on (`team_id`)

### `assignment_submission_files`

Columns:

- `id`
- `submission_id`
- `file_url`
- `created_at`

### `assignment_ai_analyses`

Purpose:

- summarized AI analysis for subjective/descriptive assignment answers

Columns:

- `id`
- `submission_id`
- `understanding_score`
- `summary`
- `missing_concepts` (`jsonb`)
- `misconceptions` (`jsonb`)
- `analyzed_at`

## 5. Quizzes

### `quizzes`

Columns:

- `id`
- `course_id`
- `title`
- `due_at`
- `created_by`
- `created_at`
- `updated_at`

### `quiz_questions`

Columns:

- `id`
- `quiz_id`
- `question_type` (`MULTIPLE_CHOICE`, `SUBJECTIVE`)
- `question_text`
- `choices_json` (`jsonb`)
- `answer_key`
- `score`
- `sort_order`

### `quiz_submissions`

Columns:

- `id`
- `quiz_id`
- `course_id`
- `student_user_id`
- `objective_score`
- `subjective_score`
- `total_score`
- `status`
- `submitted_at`
- `updated_at`

### `quiz_submission_answers`

Columns:

- `id`
- `quiz_submission_id`
- `question_id`
- `selected_choice`
- `answer_text`
- `is_correct`
- `awarded_score`
- `created_at`

### `quiz_ai_analyses`

Columns:

- `id`
- `quiz_submission_id`
- `summary`
- `understanding_score`
- `feedback`
- `analyzed_at`

## 6. AI Follow-up Questions

### `ai_follow_up_questions`

Columns:

- `id`
- `course_id`
- `content_id`
- `student_user_id`
- `context_type` (`CONTENT`, `QUIZ`, `ASSIGNMENT`)
- `source_text`
- `question_text`
- `difficulty_level`
- `created_at`

### `ai_follow_up_responses`

Columns:

- `id`
- `question_id`
- `student_user_id`
- `answer_text`
- `response_delay_seconds`
- `submitted_at`

### `ai_follow_up_analyses`

Columns:

- `id`
- `question_id`
- `response_id`
- `understanding_score`
- `feedback`
- `analyzed_at`

## 7. Teams / Collaboration

### `teams`

Columns:

- `id`
- `course_id`
- `name`
- `status` (`ACTIVE`, `RECONFIGURED`, `ARCHIVED`)
- `created_at`
- `updated_at`

### `team_members`

Columns:

- `id`
- `team_id`
- `user_id`
- `joined_at`
- `left_at`
- `status` (`ACTIVE`, `REMOVED`)

Indexes:

- index on (`team_id`, `status`)
- index on (`user_id`)

### `team_chat_rooms`

Columns:

- `id`
- `team_id`
- `created_at`

Unique:

- unique (`team_id`)

### `team_chat_messages`

Columns:

- `id`
- `chat_room_id`
- `team_id`
- `sender_user_id`
- `message_body`
- `sent_at`

Indexes:

- index on (`chat_room_id`, `sent_at`)
- index on (`team_id`, `sender_user_id`)

### `team_activity_summaries`

Columns:

- `id`
- `team_id`
- `user_id`
- `message_count`
- `feedback_count`
- `task_contribution_score`
- `contribution_score`
- `updated_at`

Unique:

- unique (`team_id`, `user_id`)

## 8. Analytics / Recommendations

### `daily_analytics_snapshots`

Purpose:

- persisted daily score result per learner and course

Columns:

- `id`
- `course_id`
- `student_user_id`
- `snapshot_date`
- `learning_sincerity_score`
- `understanding_score`
- `engagement_score`
- `collaboration_score`
- `risk_score`
- `risk_level`
- `reasons_json` (`jsonb`)
- `evidence_window_start`
- `evidence_window_end`
- `scoring_version`
- `computed_at`

Unique:

- unique (`course_id`, `student_user_id`, `snapshot_date`)

### `student_recommendations`

Columns:

- `id`
- `course_id`
- `student_user_id`
- `snapshot_date`
- `review_concepts_json` (`jsonb`)
- `recommended_contents_json` (`jsonb`)
- `recommended_actions_json` (`jsonb`)
- `created_at`

### `instructor_interventions`

Columns:

- `id`
- `course_id`
- `student_user_id`
- `instructor_user_id`
- `type` (`COUNSELING`, `SUPPLEMENT_MATERIAL`, `EXTRA_ASSIGNMENT`)
- `title`
- `message`
- `resource_urls_json` (`jsonb`)
- `created_at`

## 9. Notifications

### `notifications`

Columns:

- `id`
- `user_id`
- `type`
- `title`
- `content`
- `is_read`
- `created_at`
- `read_at`

Indexes:

- index on (`user_id`, `is_read`, `created_at`)

## Initial JPA Aggregate Suggestions

- `User`
- `Course`
- `CourseWeek`
- `Content`
- `Assignment`
- `Quiz`
- `Team`
- `DailyAnalyticsSnapshot`

Keep `learning_events` and summary tables in separate packages because their write/read patterns differ from pure domain aggregates.

## MVP Implementation Order

1. `users`, `courses`, `course_enrollments`, `course_weeks`, `contents`
2. `learning_events`, `content_progress_summaries`, `attendance_summaries`
3. `assignments`, `assignment_submissions`, `quizzes`, `quiz_submissions`
4. `teams`, `team_members`, `team_chat_messages`
5. `daily_analytics_snapshots`, `student_recommendations`, `instructor_interventions`

## Open Decisions

- split `learning_events` by family only if scale or query pressure justifies it later
- chat attachments are out of MVP scope
- define retention policy for old snapshots and raw events
