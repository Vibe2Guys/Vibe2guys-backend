# Analytics Domain

## Goal

Produce explainable learner-state outputs from transactional and event data.

## Score Families

- diligence_score
- understanding_score
- engagement_score
- collaboration_score
- dropout_risk_score

## Required Output Shape

- score
- level
- reasons
- evidence_window
- computed_at
- scoring_version

## Rules

- Scores must be reproducible from stored inputs.
- Reasons should be understandable by instructors and learners.
- Risk output must never be presented without supporting evidence.
- Scores are recomputed once per day and persisted.
- AI-generated interpretation can enrich results, but the base score must come from backend rules.
- Original learner response text may be referenced for subjective understanding analysis.

## Calculation Cadence

Daily batch calculation is the MVP policy.

Outputs should be stored per student and course so dashboard reads do not depend on live recomputation.

## Data Inputs

- attendance summary
- content progress summary
- assignment submission history
- quiz results
- subjective answer analysis summary
- follow-up question analysis summary
- team participation summary
- intervention history

## Storage Policy

Persist:

- score outputs
- level outputs
- reasons
- evidence window
- scoring version
- computed timestamp

Do not require full AI prompt logs for analytics reads in MVP.

## Visibility Principle

- students can view their own summary scores and coaching-style feedback
- instructors can view per-student scores, evidence, and intervention recommendations within owned courses
- admins can manage scoring policy but do not need learner-facing wording by default

## Open Questions

- Whether daily calculation should be course-local or global batch first
- How much historical score snapshot retention is needed in MVP
