# AGENTS.md

## Purpose

This repository is the backend workspace for an AI-driven learning operations platform.
The backend must prioritize fast MVP delivery, stable domain boundaries, and traceable learning analytics.

## Product One-Liner

Build an AI LMS that reads learner state from course, assignment, live-session, and team-activity data, then suggests interventions for instructors and learners.

## Backend Mission

- Own the backend architecture, domain model, APIs, analytics pipeline, and AI integration points.
- Ship an MVP that works with rules-based scoring first and LLM-assisted features second.
- Preserve a clean contract for frontend collaboration.

## Engineering Principles

- Prefer modular backend boundaries over large all-in-one features.
- Store raw learning events before trying to optimize analytics.
- Make analytics explainable. Every score should have reasons.
- Keep AI outputs optional and auditable. Core flows must still work without LLM output.
- Design for asynchronous processing where analytics or AI latency would block user flows.

## MVP Scope

The first backend milestone must include:

- Authentication and role separation for student, instructor, and admin
- Course enrollment and lecture content delivery metadata
- Attendance, watch-progress, assignment submission, and quiz result tracking
- Team creation and team activity event logging
- Rules-based learner scoring
- Instructor dashboard APIs
- Minimal AI endpoints for follow-up question generation and short-answer analysis

## Non-Goals For MVP

- Full machine learning pipeline
- Real-time recommendation ranking infrastructure
- Deep collaboration graph analytics
- Complex policy engines for institutions

## Required Documentation

Before major implementation, keep these documents updated:

- `ARCHITECTURE.md`
- `docs/adr/*`
- `docs/domain/*`
- `docs/api/*`
- `docs/exec-plans/active/*`

## Expected Module Boundaries

- Auth
- Users
- Courses
- Learning Events
- Assignments
- Teams
- Analytics
- AI
- Instructor Dashboard
- Admin

## API Design Rules

- Use REST for product-facing APIs.
- Use clear resource naming and role-aware endpoints.
- Return analytics with both `score` and `reasons`.
- Separate synchronous write APIs from asynchronous analytics generation where needed.
- Version APIs once frontend integration begins.

## Data Rules

- Every important learner action should be captured as an event.
- Event schemas must be append-friendly and auditable.
- Derived analytics must be reproducible from stored inputs.
- Avoid hard-deleting academic records unless compliance requires it.

## Collaboration Rules

- Frontend contracts must be documented in `docs/api`.
- Domain language must be defined in `docs/domain` before broad implementation.
- Technical decisions that may change later belong in ADRs, not scattered comments.
- Execution plans must break work into small, testable tasks suitable for vibe coding.

## Agent Structure

This repository may use role-based backend agents.

Agent definitions live in:

- `agents/architect-agent.md`
- `agents/auth-security-agent.md`
- `agents/learning-analytics-agent.md`
- `agents/course-delivery-agent.md`
- `agents/performance-test-agent.md`

Usage rules:

- `architect-agent` owns architecture, ADRs, package structure, and cross-module consistency.
- `auth-security-agent` owns authentication, authorization, token lifecycle, and security guardrails.
- `learning-analytics-agent` owns event logging, summaries, batch scoring, and analytics APIs.
- `course-delivery-agent` owns course, content, assignment, quiz, team, and chat product flows.
- `performance-test-agent` owns load-test scenarios, latency targets, and bottleneck analysis.

Coordination rules:

- Before implementation or document changes, review relevant repository `.md` files first.
- Treat repository Markdown documents as the working source of product, domain, API, and architecture context.
- If required information is missing, ambiguous, or conflicting, stop and ask the user targeted questions before proceeding.
- Architecture and ADR changes should be reviewed against `architect-agent` rules first.
- Shared API contracts must remain in `docs/api/*` as the source of truth.
- Cross-module schema changes must update `docs/generated/db-schema.md`.
- Performance work should not redefine product behavior; it validates and hardens agreed behavior.

## Quality Bar

- Authentication and authorization come before convenience features.
- Input validation is required on all write endpoints.
- Instructor-facing risk scores must include evidence.
- Background jobs must be idempotent where possible.
- Logs must support debugging of learning-event ingestion and analytics calculations.
- Query and persistence code must avoid SQL injection by using parameterized access patterns only.
- Secrets and tokens must not be logged or hardcoded beyond local development defaults.
- User-generated text and AI-generated text must be treated as untrusted content.
