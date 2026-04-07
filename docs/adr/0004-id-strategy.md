# ADR 0004: ID Strategy

## Status

Accepted

## Context

The backend needs a single primary key strategy before JPA entities, migrations, and API DTOs are implemented.
The MVP is a modular monolith running on Spring Boot with PostgreSQL.

## Decision

Use `bigint` as the primary key strategy across MVP tables.

Recommended implementation direction:

- database primary key type: `bigint`
- JPA entity ID type: `Long`
- generation strategy: one consistent DB-backed strategy per table set

## Rationale

- simpler JPA and Hibernate mapping
- easier local debugging and admin inspection
- smaller indexes and straightforward joins
- good fit for a single-service MVP

## Consequences

- IDs are easier to read and debug but more predictable than UUIDs
- if public ID exposure later becomes a concern, introduce separate public identifiers rather than changing primary keys
- all schema drafts and entity documents should assume `Long` IDs
