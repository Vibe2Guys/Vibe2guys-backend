# Spring Boot Package Structure

## Purpose

This document defines the recommended package layout for the backend MVP.
The goal is to keep the project as a modular monolith with clear boundaries that can still move fast.

## Recommended Base Package

`com.vibe2guys.backend`

## Top-Level Structure

```text
com.vibe2guys.backend
├── common
├── auth
├── user
├── course
├── learning
├── assignment
├── quiz
├── team
├── analytics
├── ai
├── dashboard
└── admin
```

## Package Responsibilities

### `common`

Shared technical concerns only.

Suggested subpackages:

- `config`
- `security`
- `exception`
- `response`
- `util`

Contents:

- JWT filter and token utilities
- global exception handler
- API response wrapper
- base audit fields if used

### `auth`

Authentication and token issuance.

Suggested classes:

- `AuthController`
- `AuthService`
- `AuthCommandService`
- `JwtTokenProvider`
- `LoginRequest`
- `RegisterRequest`

### `user`

User identity and current-user profile operations.

Suggested packages:

- `controller`
- `service`
- `domain`
- `repository`
- `dto`

Key entities:

- `User`

### `course`

Course tree and enrollment ownership.

Key entities:

- `Course`
- `CourseInstructor`
- `CourseEnrollment`
- `CourseWeek`
- `Content`

Suggested services:

- `CourseQueryService`
- `CourseCommandService`
- `EnrollmentService`
- `ContentService`

### `learning`

Raw learning events and summary tables.

Key entities:

- `LearningEvent`
- `AttendanceSummary`
- `ContentProgressSummary`
- `CourseLearningSummary`

Suggested services:

- `LearningEventService`
- `ProgressTrackingService`
- `AttendanceService`
- `LearningSummaryService`

This package should stay separate from `analytics`.
Reason:

- `learning` is operational tracking
- `analytics` is derived interpretation

### `assignment`

Assignments and submissions.

Key entities:

- `Assignment`
- `AssignmentSubmission`
- `AssignmentSubmissionFile`
- `AssignmentAiAnalysis`

### `quiz`

Quiz structure and submission results.

Key entities:

- `Quiz`
- `QuizQuestion`
- `QuizSubmission`
- `QuizSubmissionAnswer`
- `QuizAiAnalysis`

### `team`

Teams, membership, chat, participation summaries.

Key entities:

- `Team`
- `TeamMember`
- `TeamChatRoom`
- `TeamChatMessage`
- `TeamActivitySummary`

### `analytics`

Daily scoring, recommendation generation, and persisted snapshots.

Key entities:

- `DailyAnalyticsSnapshot`
- `StudentRecommendation`
- `ScoringPolicy` if later needed

Suggested services:

- `AnalyticsBatchService`
- `LearnerScoreCalculator`
- `RiskScoreCalculator`
- `RecommendationService`

### `ai`

LLM-facing assistive workflows and AI result persistence.

Key entities:

- `AiFollowUpQuestion`
- `AiFollowUpResponse`
- `AiFollowUpAnalysis`

Suggested services:

- `FollowUpQuestionService`
- `SubjectiveAnswerAnalysisService`
- `InterventionRecommendationService`

### `dashboard`

Read-focused aggregation layer for frontend screens.

Reason:

- dashboard endpoints often combine multiple module outputs
- keeping this as a thin orchestration layer avoids bloating domain services

Suggested controllers:

- `StudentDashboardController`
- `InstructorDashboardController`
- `ReportController`

### `admin`

Admin-only APIs.

Suggested responsibilities:

- user management
- analytics policy configuration
- course-level administrative operations

## Internal Layering Rule

Within each module, prefer this structure:

```text
module
├── controller
├── dto
├── domain
├── repository
└── service
```

Optional:

- `mapper`
- `scheduler`
- `client`

## Entity Placement Rule

- put JPA entities in each module's `domain` package
- do not create one giant global `entity` package
- keep repositories close to their owning aggregate

## API DTO Rule

- request/response DTOs stay in the module that owns the endpoint
- dashboard DTOs stay in `dashboard.dto`
- never expose JPA entities directly to controllers

## Batch / Scheduler Placement

Use a dedicated scheduler package where necessary:

```text
analytics
├── scheduler
└── service
```

Initial scheduled jobs:

- daily learner score calculation
- recommendation materialization

## Suggested First Entity Set

Build first:

- `User`
- `Course`
- `CourseEnrollment`
- `CourseInstructor`
- `CourseWeek`
- `Content`

Build second:

- `LearningEvent`
- `ContentProgressSummary`
- `AttendanceSummary`

Build third:

- `Assignment`
- `AssignmentSubmission`
- `Quiz`
- `QuizSubmission`

Build fourth:

- `Team`
- `TeamMember`
- `TeamChatMessage`

Build fifth:

- `DailyAnalyticsSnapshot`
- `AiFollowUpQuestion`
- `AiFollowUpResponse`

## Discussion Needed Later

- whether `assignment` and `quiz` should merge into one `assessment` module
- whether `dashboard` stays as its own package or remains thin query services inside modules
- whether `analytics` batch logic eventually moves to a separate worker application
