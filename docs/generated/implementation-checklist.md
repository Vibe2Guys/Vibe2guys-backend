# MVP Implementation Checklist

## Purpose

This checklist is the first execution guide for actual backend implementation.

## Phase 1. Foundation

- create Spring Boot project
- configure PostgreSQL connection
- configure profile-based `application.yml`
- add common response wrapper
- add error code enum and global exception handler
- add base security configuration

## Phase 2. Auth

- create `User` entity
- create `RefreshToken` entity
- create `UserRepository`
- implement password encoding
- implement register API
- implement login API
- implement refresh API
- implement current user lookup
- implement logout or token revocation API

## Phase 3. Course

- create `Course` entity
- create `CourseInstructor` entity
- create `CourseEnrollment` entity
- create `CourseWeek` entity
- create `Content` entity
- implement my courses API
- implement course enrollment API
- implement course detail API

## Phase 4. Learning Tracking

- create `LearningEvent` entity
- create `ContentProgressSummary` entity
- create `AttendanceSummary` entity
- implement content progress save API
- implement content progress read API
- implement attendance enter API
- implement attendance leave API

## Phase 5. Assessment

- create assignment entities
- create quiz entities
- implement assignment submission API
- implement quiz submission API
- persist subjective answer text

## Phase 6. Team

- create team entities
- implement team list API
- implement chat message save API
- implement contribution summary persistence

## Phase 7. Analytics

- create `DailyAnalyticsSnapshot` entity
- implement daily scheduler
- implement score calculation services
- implement student dashboard analytics API
- implement instructor dashboard analytics API

## Phase 8. AI

- create follow-up question entities
- implement follow-up question generation flow
- implement response storage
- implement summarized analysis persistence

## Acceptance Check

- auth with refresh token works
- course enrollment works
- content progress is stored in raw event and summary form
- subjective answer text is stored
- team chat text is stored
- daily snapshot can be generated for at least one course
