# Performance Test Plan

## Goal

Validate MVP performance for the highest-frequency backend paths before frontend integration and demo usage.

## Primary Scenarios

### 1. Auth

- login burst
- refresh burst
- expected concern: throttle logic and token persistence

### 2. Course Reads

- course detail
- week content list
- expected concern: repeated roster/instructor lookups

### 3. Learning Writes

- content progress writes
- live attendance start/finish
- expected concern: high write frequency into summaries and raw events

### 4. Team Chat

- chat message writes
- team analytics reads
- expected concern: repeated count queries and conversation aggregation

### 5. Analytics

- student dashboard
- instructor dashboard
- score distribution
- expected concern: on-demand snapshot generation and repeated cross-table reads

## Initial Targets

- p95 read latency: under `500ms`
- p95 write latency: under `800ms`
- error rate: under `1%`
- dashboard endpoints must not trigger obvious N+1 explosions under a small class load

## Tooling

- `k6` script: [perf/k6/mvp-scenarios.js](/Users/youngrok/Project/VibeCoding/Vibe2guys-backend/perf/k6/mvp-scenarios.js)
- local DB with representative seed data
- separate student and instructor tokens

## Local Run

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e STUDENT_ACCESS_TOKEN=... \
  -e INSTRUCTOR_ACCESS_TOKEN=... \
  -e COURSE_ID=1 \
  -e CONTENT_ID=1 \
  -e CHAT_ROOM_ID=1 \
  perf/k6/mvp-scenarios.js
```

## Review Points

- inspect slow SQL for dashboard and analytics endpoints
- check write amplification on `learning_events`, `content_progress_summaries`, `team_chat_messages`
- verify login throttle does not produce lock contention
- confirm batch snapshot generation is not accidentally triggered too often during reads

## Next Step

After first run, add scenario-specific thresholds and DB query profiling results.
