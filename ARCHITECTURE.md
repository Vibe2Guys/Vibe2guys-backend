# ARCHITECTURE.md

## Overview

This backend supports an AI learning operations platform that combines LMS workflows with learner-state analytics and intervention recommendations.

The system is designed around two layers:

1. Transactional LMS workflows
2. Analytics and AI interpretation built on captured learning events

The architectural priority is to capture reliable domain events first, then derive explainable scores and AI suggestions from those events.

## Primary Users

- Student
- Instructor
- Admin

## Core Architectural Goals

- Deliver MVP quickly with stable domain boundaries
- Track learner activity with an auditable event model
- Compute explainable learner scores
- Support AI-assisted analysis without making core flows dependent on LLM availability
- Keep frontend integration simple through explicit API contracts

## System Context

The backend owns:

- identity and access control
- course and lecture metadata
- enrollments
- attendance and watch tracking
- assignments, quizzes, and submissions
- team membership and collaboration events
- learner analytics
- AI-generated follow-up prompts and intervention suggestions

External systems may later include:

- video/live-class platform
- object storage
- LLM provider
- notification service

## Module Boundaries

### 1. Auth

- signup, login, token lifecycle
- role-based access control
- session and identity verification

### 2. Users

- student, instructor, and admin profiles
- course membership references

### 3. Courses

- courses, sections, lecture units, release policies
- enrollment and roster management

### 4. Learning Events

- attendance events
- video watch and playback events
- question-response timing events
- login and activity heartbeat events

### 5. Assignments

- assignments and quizzes
- submission records
- grading and feedback metadata

### 6. Teams

- team creation and membership
- team assignments
- collaboration activity records

### 7. Analytics

- diligence score
- understanding score
- engagement score
- collaboration score
- dropout-risk score

### 8. AI

- follow-up question generation
- short-answer understanding analysis
- intervention recommendation generation

### 9. Dashboard

- instructor summary views
- learner report views
- risk and bottleneck breakdowns

### 10. Admin

- course lifecycle administration
- user/account management
- analytics policy configuration

## MVP Architecture

The MVP should use a modular monolith with clear internal boundaries.

Reasoning:

- one backend engineer can move faster
- domain logic stays in one deployable unit
- future extraction is still possible if boundaries stay clean

Recommended high-level shape:

- API application layer
- domain services per module
- relational database for transactional state
- append-oriented event tables for learning telemetry
- background job runner for analytics and AI tasks

## Data Model Strategy

Use two data categories:

### Transactional Data

- users
- courses
- lectures
- enrollments
- assignments
- submissions
- teams

### Event Data

- attendance logs
- watch progress logs
- answer interaction logs
- team activity logs
- system notifications and interventions

Derived learner-state tables may cache computed outputs, but event data remains the source of truth for analytics recomputation.

## Analytics Strategy

Start with rules-based scoring.

Initial scores:

- diligence: attendance rate, watch completion, assignment submission rate
- understanding: quiz score, short-answer analysis summary
- engagement: response delay, watch repetition, learning continuity
- collaboration: team chat participation, team task contribution, peer response activity
- dropout risk: recent decline in attendance, submissions, response speed, and team activity

Every analytics result should store:

- score
- level
- reasons
- source window
- computed_at

## AI Strategy

AI is an assistive layer, not the system core.

MVP AI responsibilities:

- generate follow-up questions from lesson context
- summarize short-answer quality signals
- generate intervention suggestions for instructor or learner

Constraints:

- raw event capture must not depend on AI availability
- AI outputs should be stored with prompt context and timestamps
- critical decisions should not rely solely on opaque model output

## Request and Processing Flow

### Synchronous flows

- auth
- course browsing
- enrollment checks
- assignment submission
- lecture progress writes

### Asynchronous flows

- daily learner score batch calculation
- daily risk signal materialization
- AI follow-up question generation
- intervention recommendation generation

Typical flow:

1. user action hits API
2. transactional state is written
3. learning event is appended
4. summary tables are updated where needed for fast operational reads
5. daily batch job recalculates learner analytics snapshots
6. dashboard/report endpoints read persisted summaries and daily derived results

## Authorization Model

- Student: view own courses, progress, reports, team activities
- Instructor: manage own courses, inspect enrolled learners, trigger interventions
- Admin: system-wide course, account, and policy management

Object-level access control is required for course-scoped resources.

## Reliability Requirements

- event ingestion endpoints must be safe against duplicates where feasible
- analytics jobs should be retryable
- derived scores should include versioning to support rule changes
- instructor dashboards should degrade gracefully if AI output is delayed

## Security Requirements

- protect all write endpoints with authentication
- validate role and course ownership on instructor/admin actions
- minimize personal data collection
- separate sensitive learner records from general activity logs where practical
- record audit logs for instructor/admin interventions

## Observability

Track at minimum:

- API request success and error rates
- event ingestion counts by type
- analytics job failures
- AI task latency and failure rate
- dashboard read latency

## Initial Open Decisions

The following are intentionally deferred to ADRs:

- exact backend framework and language
- auth token strategy
- event table schema and partitioning approach
- background job technology
- LLM provider abstraction
