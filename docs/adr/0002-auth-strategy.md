# ADR 0002: Auth Strategy

## Status

Accepted

## Context

The platform has three main roles: student, instructor, and admin.
The backend needs secure authentication plus course-scoped authorization.

## Decision

Use local authentication with `Spring Security` and `JWT` for the MVP.

MVP direction:

- email/password login
- short-lived access token
- refresh token support included in MVP
- role-based authorization with course ownership checks
- user identity resolved from JWT subject

Refresh token direction:

- persist refresh tokens in DB
- support token rotation
- allow token revocation on logout or suspicious session invalidation

## Key Questions

- How will password reset and email verification be handled after MVP?
- Is institution SSO required in a later phase?

## Consequences

- Authorization rules must be applied consistently across course, team, analytics, and admin APIs.
- Instructor access must be course-scoped, not global by role alone.
- Spring Security filter chain and exception responses should be standardized early.
- Flutter clients should only need bearer-token handling for protected APIs in MVP.
- Refresh token persistence and revocation must be implemented as part of the auth module.
