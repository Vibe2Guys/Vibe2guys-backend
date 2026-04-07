# ADR 0001: Tech Stack

## Status

Accepted

## Context

The backend is being built by a small team with one primary backend owner.
The platform needs fast MVP delivery, a clean API contract, auditable analytics, and future AI integration.
The frontend will be implemented separately in Flutter, so the backend must expose stable mobile-friendly REST APIs.

## Decision

Use `Spring Boot` as the backend framework for the MVP.

Recommended MVP stack:

- `Java 21`
- `Spring Boot`
- `Spring Web`
- `Spring Security`
- `Spring Data JPA`
- `PostgreSQL`
- `JWT`-based authentication
- `Bean Validation`
- `Springdoc OpenAPI`

Optional additions after MVP foundation:

- `QueryDSL` or `jOOQ` for complex analytics queries
- `Redis` for caching or token/session support
- `Spring Batch` or `Quartz` for scheduled analytics jobs
- message queue only if async load grows beyond simple scheduler/job patterns

## Rationale

- Spring Boot is a strong fit for a modular monolith.
- Validation, security, transaction handling, and persistence are mature and predictable.
- It is well suited for role-based APIs, scheduled analytics, and future background processing.
- The backend engineer can structure modules cleanly without introducing distributed-system complexity too early.
- Flutter integration is straightforward through REST and JSON contracts.

## Consequences

- The chosen stack must support event ingestion, analytics jobs, and role-based APIs without heavy platform overhead.
- The project should standardize request and response DTOs early.
- Security configuration and exception handling should be made consistent across all modules.
- OpenAPI generation should be kept in sync with `docs/api/*`.
