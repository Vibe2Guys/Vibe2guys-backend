# Courses Domain

## Core Entities

- Course
- CourseWeek
- Content
- Enrollment
- CourseInstructor

## Rules

- A course is owned by one or more instructors.
- Students access content only through enrollment.
- Course content may be fully open or released by schedule.
- `LIVE` is modeled as a `Content` type, not a separate top-level entity.

## Key Behaviors

- create and update courses
- enroll users
- publish content
- control weekly release

## Content Types

- `VOD`
- `LIVE`
- `DOCUMENT`

## Release Policy

- `is_sequential_release = false`: all opened content can be accessed immediately
- `is_sequential_release = true`: access follows each week or content `open_at`

## Enrollment Policy

Current inference from API design:

- students can self-enroll through `POST /api/v1/courses/{courseId}/enrollments`
- instructors and admins can create and manage courses

If enrollment policy changes later, this should move to an ADR.

## Deferred

- waitlist support
- invite-code based course entry
- section splitting beyond one course/week/content tree
