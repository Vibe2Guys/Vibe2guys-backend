# Users Domain

## Core Entities

- User
- CourseEnrollment
- CourseInstructor

## Rules

- A user has exactly one platform identity.
- A user may act in different roles depending on course membership and system role.
- Instructor and admin permissions must be explicit.
- MVP stores user profile in a single `users` table rather than separate profile tables.

## Key Fields

- user_id
- email
- password_hash
- display_name
- global_role
- status
- profile_image_url
- created_at

## Authorization Interpretation

- `global_role` defines platform-level authority.
- Course-scoped access is determined by `course_enrollments` and `course_instructors`.
- Instructor permissions must always be checked against course ownership.

## MVP Decision

- Local auth with email and password
- Single profile model
- No institutional metadata in MVP

## Deferred

- institutional student number or faculty metadata
- external SSO identity mapping
