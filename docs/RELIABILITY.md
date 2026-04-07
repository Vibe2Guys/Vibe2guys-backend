# RELIABILITY

## Purpose

This document defines the MVP backend reliability baseline.

## Core Principle

The platform must prioritize correctness of learner records over aggressive real-time behavior.

## Write Reliability

- auth writes must be transactional
- assignment and quiz submissions must be durable
- learning event writes should be append-safe
- summary table updates should be retryable when derived from raw events

## Read Reliability

- dashboards should read persisted summaries and snapshots
- analytics reads must degrade gracefully if the latest batch has not run yet
- AI-derived fields should not block core LMS screens

## Batch Reliability

- daily analytics jobs must be idempotent
- use unique keys to prevent duplicate daily snapshots
- partial failures should be logged and rerunnable

## Error Handling

- use standardized API error responses
- separate validation failures from permission failures from system failures
- record enough structured logs to trace failed writes and failed batch computations

## Recovery Approach

- raw learning events are the source for recalculating summaries
- persisted daily snapshots can be recomputed for a date range if scoring rules change
- refresh token revocation data must survive restarts

## Monitoring Baseline

Track at minimum:

- authentication failure rate
- token refresh failure rate
- learning event ingestion count by type
- assignment and quiz submission failure count
- daily analytics batch success and failure
- dashboard API latency

## Deferred

- distributed locking for jobs
- multi-region resilience
- queue-based event processing
