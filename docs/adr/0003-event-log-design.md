# ADR 0003: Event Log Design

## Status

Accepted

## Context

Learner-state analysis depends on reliable event capture across attendance, video engagement, assignments, quizzes, and team activity.
The frontend needs fast reads, but the backend also needs enough raw evidence to recalculate analytics later.

## Decision

Use append-oriented learning event storage with separate summary tables for fast reads.

The MVP event design is:

- `LIVE` classes are modeled as a `Content` type, not a separate top-level entity
- VOD and live learning activity use `event storage + summary storage`
- team chat stores message body as well as metadata
- subjective learner answers and AI follow-up answers are stored as original text
- AI analysis stores summarized results and scores, not full raw prompt history
- learner scores are recomputed once per day and persisted

## Event Record Shape

Each event should contain at minimum:

- `event_id`
- `event_type`
- `actor_user_id`
- `course_id`
- `week_id` if applicable
- `content_id` if applicable
- `resource_type`
- `resource_id`
- `occurred_at`
- `payload_json`
- `schema_version`

## Required Event Families

- attendance events
- watch progress events
- quiz interaction events
- assignment submission events
- team participation events
- intervention events

## Summary Storage

Because the frontend needs fast dashboard reads, maintain summary tables alongside raw events.

Examples:

- content progress summary per student/content
- attendance summary per student/live content
- course learning summary per student/course
- collaboration summary per member/team
- daily learner analytics summary per student/course

## Modeling Decisions

### Live Class Modeling

`LIVE` is one content type within the course content model.

Reasoning:

- keeps course, week, and content structure simple
- avoids duplicate release and ownership logic
- allows attendance and live participation to reuse content-scoped analytics

### Video / Content Tracking

Store both:

- raw playback and progress events
- latest summary state for fast reads

Raw events support replay hotspot analysis, drop-off analysis, and later rules changes.

### Team Chat Storage

Store message body and metadata.

Minimum message metadata:

- sender
- team
- chat room
- sent time
- message body

This is required for collaboration analysis and instructor evidence views.

### Learner Answer Storage

Store the original learner response text for:

- subjective assignment answers
- subjective quiz answers
- AI follow-up question answers

For other AI outputs, store summarized analysis results and scores rather than full raw prompt logs in MVP.

### Analytics Calculation Cadence

Learner scores are recalculated once per day and persisted.

This batch-style model is preferred for MVP because:

- dashboard reads stay predictable
- scoring logic remains simple
- one backend engineer can operate it without building a complex event-driven pipeline early

## Consequences

- Event schemas must remain stable enough for analytics recomputation.
- Payload shape should be versioned if event formats evolve.
- Write paths must update both raw events and the relevant summary records where applicable.
- Daily analytics jobs must record `computed_at` and `scoring_version`.
