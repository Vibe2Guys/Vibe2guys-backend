# Batch Scheduling Design

## Purpose

This document defines the MVP scheduling strategy for daily analytics calculation and recommendation materialization.

## MVP Policy

- learner scores are recomputed once per day
- results are stored in persistent snapshot tables
- dashboards read persisted outputs instead of recalculating live

## Recommended Scheduler

For MVP, use Spring scheduling in the application itself.

Reasoning:

- simple to operate
- no separate worker deployment needed
- good fit for one-backend-engineer scope

Possible later upgrades:

- Quartz
- Spring Batch
- separate analytics worker

## Daily Jobs

### 1. Learner Score Snapshot Job

Purpose:

- compute daily learner scores per course

Inputs:

- attendance summaries
- content progress summaries
- assignment submissions
- quiz submissions
- team activity summaries
- AI analysis summaries

Outputs:

- `daily_analytics_snapshots`

### 2. Recommendation Materialization Job

Purpose:

- create learner-facing recommended actions and review concepts

Outputs:

- `student_recommendations`

### 3. Intervention Suggestion Job

Purpose:

- prepare instructor-facing intervention candidates if needed

Outputs:

- derived recommendations or materialized recommendation rows

## Scheduling Rule

Initial recommendation:

- run once daily during low-traffic early morning window
- use one timezone policy consistently for academic reporting

Discussion needed:

- should academic batch time follow Korea time only or course/institution-local time later

## Idempotency Rule

- each daily job must be safe to rerun for the same date
- use unique keys like (`course_id`, `student_user_id`, `snapshot_date`) to avoid duplicate snapshots
- prefer upsert semantics for daily outputs

## Failure Handling

- log failed course or student batches with enough identifiers to rerun
- avoid failing the entire job because of one learner record
- expose admin-visible monitoring later if needed

## Suggested Implementation Shape

```text
analytics
├── scheduler
│   ├── AnalyticsSnapshotScheduler
│   └── RecommendationScheduler
├── service
│   ├── AnalyticsBatchService
│   ├── LearnerScoreCalculator
│   ├── RiskScoreCalculator
│   └── RecommendationService
└── repository
```

## Calculation Flow

1. load active courses
2. load enrolled students per course
3. gather required summary inputs
4. calculate scores
5. derive reasons and levels
6. persist snapshot rows
7. persist recommendations

## Deferred

- partial intra-day recalculation
- queue-based analytics pipeline
- distributed job locking
