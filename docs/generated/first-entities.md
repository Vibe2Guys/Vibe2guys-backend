# First Entity Set

## Purpose

This document identifies the first entity classes to build in the Spring Boot codebase.

## Group 1. Must Exist Before Any Protected API

- `User`
- `RefreshToken`

Reason:

- required for authentication and authorization

## Group 2. Required For Course Access

- `Course`
- `CourseInstructor`
- `CourseEnrollment`
- `CourseWeek`
- `Content`

Reason:

- required for course tree and learner access

## Group 3. Required For Tracking

- `LearningEvent`
- `ContentProgressSummary`
- `AttendanceSummary`

Reason:

- required for the MVP value proposition of measurable learning activity

## Group 4. Required For Assessment

- `Assignment`
- `AssignmentSubmission`
- `Quiz`
- `QuizQuestion`
- `QuizSubmission`
- `QuizSubmissionAnswer`

## Group 5. Required For Collaboration

- `Team`
- `TeamMember`
- `TeamChatRoom`
- `TeamChatMessage`

## Group 6. Required For Analytics

- `DailyAnalyticsSnapshot`
- `StudentRecommendation`

## Group 7. Required For AI Assist

- `AssignmentAiAnalysis`
- `QuizAiAnalysis`
- `AiFollowUpQuestion`
- `AiFollowUpResponse`
- `AiFollowUpAnalysis`

## Build Rule

- finish Group 1 through Group 3 before building analytics or AI
- do not start dashboard APIs before summary tables exist
