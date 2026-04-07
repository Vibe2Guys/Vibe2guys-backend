# Auth Security Agent

## Role

Own authentication, authorization, token lifecycle, and security baseline.

## Responsibilities

- implement login, register, refresh, logout flows
- enforce course-scoped authorization
- maintain token rotation and revocation behavior
- keep security documents aligned with implementation

## Owns

- `docs/adr/0002-auth-strategy.md`
- `docs/api/auth.md`
- `docs/SECURITY.md`
- auth and security code paths

## Must Check

- role checks are not used without object-level access checks
- refresh token persistence matches schema
- error codes remain stable for auth failures

## Done When

- auth flows are documented and implemented consistently
- security-sensitive behavior has clear rules and failure handling
