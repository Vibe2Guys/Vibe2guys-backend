# Error Code Conventions

## Purpose

This document defines API error response and error code conventions for Flutter and backend collaboration.

## Response Shape

```json
{
  "success": false,
  "message": "에러 메시지",
  "errorCode": "COURSE_NOT_FOUND"
}
```

Optional additions later:

- `details`
- `fieldErrors`
- `timestamp`

## Naming Rule

- use uppercase snake case
- prefer `DOMAIN_REASON` format

Examples:

- `AUTH_REQUIRED`
- `INVALID_CREDENTIALS`
- `COURSE_NOT_FOUND`
- `ENROLLMENT_ALREADY_EXISTS`
- `ASSIGNMENT_SUBMISSION_NOT_FOUND`

## HTTP Mapping Rule

- `400 Bad Request`: invalid input, malformed request, business precondition failure
- `401 Unauthorized`: missing or invalid token
- `403 Forbidden`: authenticated but no permission
- `404 Not Found`: target resource missing
- `409 Conflict`: duplicate or state conflict
- `422 Unprocessable Entity`: semantically invalid input if needed
- `500 Internal Server Error`: unexpected server failure

## Initial Error Code Set

### Auth

- `AUTH_REQUIRED`
- `INVALID_CREDENTIALS`
- `TOKEN_EXPIRED`
- `TOKEN_INVALID`
- `EMAIL_ALREADY_EXISTS`

### User

- `USER_NOT_FOUND`
- `USER_INACTIVE`

### Course

- `COURSE_NOT_FOUND`
- `COURSE_ACCESS_DENIED`
- `COURSE_ALREADY_ENROLLED`
- `COURSE_ENROLLMENT_NOT_FOUND`
- `CONTENT_NOT_FOUND`
- `WEEK_NOT_FOUND`

### Assignment / Quiz

- `ASSIGNMENT_NOT_FOUND`
- `ASSIGNMENT_SUBMISSION_NOT_FOUND`
- `QUIZ_NOT_FOUND`
- `QUIZ_SUBMISSION_NOT_FOUND`
- `SUBMISSION_DEADLINE_EXCEEDED`

### Team

- `TEAM_NOT_FOUND`
- `TEAM_ACCESS_DENIED`
- `CHAT_ROOM_NOT_FOUND`

### Analytics / AI

- `ANALYTICS_NOT_READY`
- `AI_ANALYSIS_NOT_FOUND`
- `FOLLOW_UP_QUESTION_NOT_FOUND`
- `FOLLOW_UP_RESPONSE_NOT_FOUND`

### Common Validation

- `INVALID_INPUT`
- `RESOURCE_CONFLICT`
- `INTERNAL_SERVER_ERROR`

## Backend Rule

- map domain exceptions to stable error codes
- do not leak stack traces or ORM exception text to clients
- keep `message` readable for frontend display or logging

## Flutter Collaboration Rule

- frontend should rely on `errorCode` for branching, not localized `message`
- message wording can change, error codes should remain stable
