# Spring Boot Project Bootstrap

## Purpose

This document defines the initial Spring Boot project structure to implement the MVP backend with minimal churn.

## Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Validation
- PostgreSQL
- Springdoc OpenAPI

## Recommended Initial Dependencies

- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- `postgresql`
- `springdoc-openapi-starter-webmvc-ui`
- test dependencies

Optional after first auth implementation:

- `jjwt` or preferred JWT library
- `lombok` only if you want it consistently

## Base Package

`com.vibe2guys.backend`

## Initial Directory Shape

```text
src/main/java/com/vibe2guys/backend
├── BackendApplication.java
├── common
│   ├── config
│   ├── exception
│   ├── response
│   ├── security
│   └── util
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

## First Common Classes

### `common.response`

- `ApiResponse`

Purpose:

- standardize success response shape

### `common.exception`

- `ErrorCode`
- `BusinessException`
- `GlobalExceptionHandler`

Purpose:

- map domain and validation failures to stable API responses

### `common.security`

- `JwtTokenProvider`
- `JwtAuthenticationFilter`
- `CustomUserDetailsService`
- `SecurityConfig`

Purpose:

- centralize JWT parsing and route protection

## First Module Classes

### `auth`

- `AuthController`
- `AuthService`
- `AuthTokenService`
- `dto/LoginRequest`
- `dto/RegisterRequest`
- `dto/TokenRefreshRequest`
- `dto/AuthResponse`

### `user`

- `domain/User`
- `repository/UserRepository`
- `service/UserService`
- `controller/UserController`

### `course`

- `domain/Course`
- `domain/CourseInstructor`
- `domain/CourseEnrollment`
- `domain/CourseWeek`
- `domain/Content`
- `repository/*`
- `service/CourseService`
- `controller/CourseController`

### `learning`

- `domain/LearningEvent`
- `domain/AttendanceSummary`
- `domain/ContentProgressSummary`
- `repository/*`
- `service/LearningEventService`
- `service/AttendanceService`
- `service/ProgressService`
- `controller/LearningController`

## First Configuration Files

### `application.yml`

Include:

- datasource config
- JPA config
- JWT secret and expiration settings
- server timezone policy

### Profiles

Recommended:

- `local`
- `test`

## First API Set To Implement

1. `POST /api/v1/auth/register`
2. `POST /api/v1/auth/login`
3. `POST /api/v1/auth/refresh`
4. `GET /api/v1/users/me`
5. `GET /api/v1/courses/my`
6. `POST /api/v1/courses/{courseId}/enrollments`
7. `POST /api/v1/contents/{contentId}/progress`
8. `GET /api/v1/contents/{contentId}/progress`

## First DB Tables To Implement

1. `users`
2. `refresh_tokens`
3. `courses`
4. `course_instructors`
5. `course_enrollments`
6. `course_weeks`
7. `contents`
8. `learning_events`
9. `content_progress_summaries`

## Implementation Notes

- start with a single `learning_events` table in MVP
- keep dashboard queries out of controllers
- avoid implementing analytics batch until auth, course, and tracking flows are stable
- add OpenAPI annotations only after DTOs stabilize enough
