# SECURITY

## Purpose

This document defines the MVP security baseline for the backend.

## Authentication

- use `Spring Security`
- use JWT access token for protected API access
- include refresh token in MVP
- store refresh tokens in DB for revocation and rotation
- never store raw refresh token values in DB; persist only a server-side hash

## Authorization

- platform roles: `STUDENT`, `INSTRUCTOR`, `ADMIN`
- role checks alone are not sufficient for course resources
- instructor access must always validate course ownership
- student access must always validate enrollment or ownership of personal data

## Password Policy

- store only hashed passwords
- use strong password encoding through Spring Security
- never log raw passwords

## Token Policy

- access token should be short-lived
- refresh token should be revocable
- logout must invalidate refresh token
- suspicious sessions should be revocable server-side

## API Protection

- all write endpoints require authentication unless explicitly public
- input validation is mandatory on every write request
- standardized error responses must not leak internal implementation details

## Secure Coding Rules

### SQL Injection

- use JPA repositories, parameter binding, or prepared statements by default
- never concatenate raw user input into SQL, JPQL, or native query strings
- if native queries are required, use bound parameters only
- search, sort, and filter inputs must be allow-listed before being mapped to query logic

### Input Handling

- validate all request DTOs with Bean Validation
- enforce size limits on text inputs to reduce abuse and unexpected load
- reject malformed enum, pagination, sort, and identifier inputs early

### XSS and Unsafe Content

- backend should treat learner text, chat text, and AI output as untrusted input
- do not store or return executable HTML as a trusted field
- if rich text is introduced later, sanitization rules must be defined explicitly

### Secrets and Sensitive Data

- never hardcode production secrets in source code
- JWT secrets, DB credentials, and API keys must come from environment or secret storage
- do not log access tokens, refresh tokens, passwords, or raw authorization headers
- deployment env files must never be committed with real values

### Error and Log Safety

- do not expose stack traces, SQL text, or ORM internals in API responses
- security-relevant logs should record actor, target, and result without leaking secrets
- authentication failure logs should be useful for auditing but not reveal whether a password or token value was correct

### Abuse and Brute Force

- repeated failed login attempts must be throttled
- token refresh abuse should be auditable and revocable
- endpoints that accept high-frequency writes, such as progress or chat events, should be reviewed for abuse controls

Current MVP rule:

- login is blocked for a cooldown window after repeated failures from the same email or client IP

## Data Protection

- minimize collection of personal data
- original learner text is stored only where needed for education and analytics
- AI analysis stores summaries and scores, not unnecessary raw prompt traces
- avoid hard deletion of academic records by default

## Audit Needs

At minimum, preserve audit visibility for:

- login and token refresh activity
- instructor interventions
- analytics batch execution time and version
- changes to analytics policy when admin features are added

## Team Chat Scope

- MVP supports text messages only
- attachments are out of scope for the first release

## Deferred

- password reset flow
- email verification
- institution SSO
- encryption-at-rest policy details
- rate limiting for high-frequency learning and chat events

## Deployment Guardrails

- do not expose PostgreSQL publicly unless there is a strong operational reason
- disable Swagger/OpenAPI endpoints in public production unless explicitly needed
- backoffice endpoints should require layered controls beyond path knowledge
- terminate TLS in a trusted reverse proxy or load balancer
