# Entity Conventions

## Purpose

This document defines shared entity and persistence conventions for the Spring Boot backend.
The goal is to keep JPA entities predictable before implementation starts.

## ID Rule

- all primary keys use `bigint`
- all JPA IDs use `Long`
- do not mix `UUID` and `Long` in MVP domain entities

## Common Audit Fields

Recommended common fields for mutable entities:

- `createdAt`
- `updatedAt`

Recommended additional fields for user-facing activity records:

- `createdBy` when ownership matters
- `updatedBy` only if actual update auditing is needed

Recommended additional fields for soft-delete candidates:

- `deletedAt`

## Naming Rule

- table names: plural snake_case
- columns: snake_case
- entity classes: singular PascalCase
- enum names: uppercase snake_case values

Examples:

- table: `course_enrollments`
- entity: `CourseEnrollment`
- enum value: `NOT_SUBMITTED`

## Base Entity Rule

It is acceptable to use a shared base entity for audit timestamps.

Recommended minimal base:

- `createdAt`
- `updatedAt`

Avoid putting business logic in a shared base entity.

## Soft Delete Rule

Prefer soft delete for:

- users
- courses
- academic submissions
- team membership history

Avoid hard delete unless:

- data is clearly temporary
- compliance or cleanup requirements demand it

## Relationship Rule

- default to lazy loading on JPA associations
- do not expose entities directly to controllers
- avoid deep bidirectional graphs unless required

Recommended approach:

- keep aggregate roots narrow
- query complex dashboard views via dedicated query services

## JSON Column Rule

Use `jsonb` only where the shape is naturally flexible.

MVP candidates:

- `learning_events.payload_json`
- analytics reasons arrays
- recommendation arrays
- AI missing concept and misconception lists

Do not overuse `jsonb` for core transactional entities.

## Status Field Rule

Use explicit status enums rather than nullable flags.

Examples:

- user status
- course status
- enrollment status
- submission status

## Time Rule

- persist timestamps with timezone-safe application handling
- use one application-wide standard for serialization
- keep date-only fields for pure academic schedule values like course start/end date

## Text Storage Rule

Store original learner text for:

- subjective assignment answers
- subjective quiz answers
- AI follow-up responses
- team chat messages

Store summarized AI outputs separately from original learner text.

## Entity Package Rule

- each module owns its entities
- do not create a single global `entity` folder
- repositories stay close to the owning entity module

## Deferred

- full audit trail framework
- optimistic locking policy
- public opaque identifiers
