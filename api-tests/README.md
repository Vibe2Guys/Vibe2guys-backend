# API Tests

## Purpose

Manual and local smoke checks for the backend API.

## Files

- `local-smoke.http`: IntelliJ HTTP Client or VS Code REST Client style request collection
- `http-client.env.json`: local variables for manual execution

## Recommended Flow

1. register or create a local student/instructor/admin account
2. login and copy `accessToken` and `refreshToken`
3. run course, assignment, quiz, analytics, and notification requests in order
4. verify expected status and response shape

## Notes

- requests use `{{baseUrl}}` and environment variables
- some endpoints depend on previously created IDs
- admin endpoints should be run with an `ADMIN` account
