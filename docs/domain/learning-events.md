# Learning Events Domain

## Goal

Capture learner actions needed for progress tracking, analytics, and AI interpretation.

## Event Families

- attendance
- watch_progress
- playback_seek
- playback_pause
- playback_complete
- quiz_attempt
- quiz_submission
- assignment_submission
- response_timing
- login_activity
- team_chat_message
- team_task_activity
- intervention_action

## Rules

- Events are append-only by default.
- Each event must identify actor, course, event type, and timestamp.
- Events should carry enough context for later analytics recomputation.
- `LIVE` sessions are represented as `Content` with type `LIVE`.
- For VOD and live participation, raw events and summary state are both maintained.

## Required Common Fields

- `event_id`
- `event_type`
- `actor_user_id`
- `course_id`
- `content_id` when relevant
- `resource_type`
- `resource_id`
- `occurred_at`
- `payload_json`
- `schema_version`

## Summary Records

In addition to raw events, the backend keeps read-optimized summaries.

Examples:

- content progress summary
- attendance summary
- student course learning summary
- team member participation summary

## Stored Original Responses

The backend stores original learner text for:

- subjective assignment submissions
- subjective quiz answers
- AI follow-up question responses

This allows instructor inspection and later recomputation of AI-assisted analysis.

## Derived Outputs

- attendance rate
- watch completion
- learning continuity
- response delay trend
- replay hotspot summary
- team participation count

## Open Questions

- Exact event table split between one generic table and per-family tables
- Whether chat attachments are needed in MVP
