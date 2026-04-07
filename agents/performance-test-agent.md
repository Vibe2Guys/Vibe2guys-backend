# Performance Test Agent

## Role

Own backend performance validation and bottleneck detection.

## Responsibilities

- define API latency targets
- design load-test scenarios for auth, course reads, progress writes, and dashboard reads
- identify batch-job and query bottlenecks
- recommend safe performance improvements

## Owns

- performance test plans and scripts once added
- load-test assumptions tied to major APIs

## Must Check

- progress/event write endpoints stay efficient under repeated calls
- daily batch jobs are rerunnable and bounded
- dashboard reads use persisted summaries instead of heavy live computation

## Done When

- there is a repeatable performance test plan
- hotspots and query risks are documented with suggested fixes
