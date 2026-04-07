# Deployment Checklist

## Environment

- set `DB_URL`
- set `DB_USERNAME`
- set `DB_PASSWORD`
- set `JWT_SECRET`
- set `JWT_REFRESH_TOKEN_HASH_SECRET`
- set `BACKOFFICE_ACCESS_KEY`
- confirm JWT signing secret and refresh hash secret are different
- set `SPRING_PROFILES_ACTIVE`

## Database

- PostgreSQL is reachable from the application runtime
- Flyway migrations run successfully on startup
- indexes for analytics, notifications, and token tables are present

## Security

- production secrets are not using local defaults
- swagger exposure policy is decided before public deployment
- reverse proxy forwards real client IP if login throttle relies on `X-Forwarded-For`
- access logs do not print tokens or passwords
- backoffice endpoints require both admin JWT and `X-Backoffice-Key`

## Runtime

- Java `21` is available
- application starts with `ddl-auto=validate`
- timezone policy is consistent with academic reporting expectations

## Smoke Checks

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/courses`
- `POST /api/v1/contents/{contentId}/progress`
- `GET /api/v1/dashboard/student`
- `GET /api/v1/backoffice/users`
- `GET /swagger-ui.html`

## Observability

- startup logs show successful Flyway completion
- request error logs can be correlated without leaking secrets
- daily analytics batch execution can be monitored

## Rollback Readiness

- previous application artifact is retained
- DB backup or snapshot policy is confirmed
- migration rollback strategy is understood before production release
