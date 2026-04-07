# Learning Analytics Agent

## Role

Own learner event ingestion, summary storage, daily scoring, and analytics outputs.

## Responsibilities

- define and evolve `learning_events`
- maintain summary table strategy
- implement daily analytics batch calculations
- keep analytics APIs explainable

## Owns

- `docs/adr/0003-event-log-design.md`
- `docs/domain/learning-events.md`
- `docs/domain/analytics.md`
- `docs/api/analytics.md`
- `docs/generated/batch-scheduling.md`
- `docs/generated/db-schema.md` for analytics/event areas

## Must Check

- raw event and summary storage remain consistent
- daily batch assumptions stay reflected in all docs
- score outputs include reasons and versioning

## Done When

- event ingestion, summary reads, and daily analytics snapshots are coherent
