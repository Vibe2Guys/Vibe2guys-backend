# MVP Backend Plan

## Goal

Deliver a backend MVP for the AI learning operations platform that supports core LMS flows, learning-event capture, explainable scoring, and minimal AI assistance.

## Phase 1

- choose backend stack
- define auth strategy
- define event schema
- create project skeleton
- define package structure and base conventions

## Phase 2

- implement auth and role model
- implement courses and enrollments
- implement assignments and submissions
- implement learning-event ingestion
- implement progress and attendance summaries

## Phase 3

- implement rules-based analytics
- implement instructor dashboard APIs
- implement learner summary APIs
- implement daily analytics batch storage

## Phase 4

- implement team module minimum scope
- implement AI follow-up question generation
- implement short-answer analysis workflow

## Concrete Build Order

### Step 1. Base project

- Spring Boot project generation
- package structure
- global exception handling
- response wrapper
- security config

### Step 2. Auth and users

- user entity
- password encoding
- login/register API
- JWT issue/validation
- refresh token persistence and rotation
- current user resolver

### Step 3. Course tree

- course entity
- course instructor mapping
- enrollment mapping
- week and content entities
- my courses API

### Step 4. Learning tracking

- learning event entity
- content progress summary
- attendance summary
- progress save API
- attendance enter/leave API

### Step 5. Assessment

- assignment and submission entities
- quiz, question, submission entities
- subjective answer storage

### Step 6. Team module

- team and team member entities
- chat room and message entities
- team activity summary

### Step 7. Analytics

- daily analytics snapshot entity
- scoring batch job
- student/instructor analytics API

### Step 8. AI assist

- follow-up question entity
- follow-up response and analysis entities
- assignment/quiz AI analysis persistence

## Exit Criteria

- student can sign in and access enrolled course metadata
- lecture and assignment events are stored
- instructor can view learner score summary with evidence
- at least one AI-assisted feature is usable end to end

## Known Discussion Points

- raw event table split strategy
- retention policy for events and snapshots
