# SECURITY

## Purpose

This document defines the MVP security baseline for the backend.

## Authentication

- use `Spring Security`
- use JWT access token for protected API access
- include refresh token in MVP
- store refresh tokens in DB for revocation and rotation

## Authorization

- platform roles: `STUDENT`, `INSTRUCTOR`, `ADMIN`
- role checks alone are not sufficient for course resources
- instructor access must always validate course ownership
- student access must always validate enrollment or ownership of personal data

## Password Policy

- store only hashed passwords
- use strong password encoding through Spring Security
- never log raw passwords

## Token Policy

- access token should be short-lived
- refresh token should be revocable
- logout must invalidate refresh token
- suspicious sessions should be revocable server-side

## API Protection

- all write endpoints require authentication unless explicitly public
- input validation is mandatory on every write request
- standardized error responses must not leak internal implementation details

## Data Protection

- minimize collection of personal data
- original learner text is stored only where needed for education and analytics
- AI analysis stores summaries and scores, not unnecessary raw prompt traces
- avoid hard deletion of academic records by default

## Audit Needs

At minimum, preserve audit visibility for:

- login and token refresh activity
- instructor interventions
- analytics batch execution time and version
- changes to analytics policy when admin features are added

## Team Chat Scope

- MVP supports text messages only
- attachments are out of scope for the first release

## Deferred

- password reset flow
- email verification
- institution SSO
- encryption-at-rest policy details
