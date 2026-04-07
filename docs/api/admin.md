# Admin API

## Scope

- 사용자 목록 조회
- 교수자/학생 계정 생성
- 분석 기준 조회/수정

## `GET /api/v1/admin/users`

관리자가 사용자 목록을 조회한다.

Query Params:

- `page`
- `size`
- `role`
- `keyword`

## `POST /api/v1/admin/users`

관리자가 학생 또는 교수자 계정을 생성한다.

Request:

```json
{
  "name": "김교수",
  "email": "prof@example.com",
  "password": "1234abcd!",
  "role": "INSTRUCTOR"
}
```

## `GET /api/v1/admin/analytics-config`

현재 규칙 기반 분석 가중치와 risk threshold를 조회한다.

## `PATCH /api/v1/admin/analytics-config`

규칙 기반 분석 가중치와 risk threshold를 수정한다.

Request:

```json
{
  "attendanceWeight": 0.15,
  "progressWeight": 0.25,
  "assignmentWeight": 0.2,
  "quizWeight": 0.25,
  "teamActivityWeight": 0.15,
  "riskThresholdHigh": 80,
  "riskThresholdMedium": 55
}
```

## Backend Notes

- 가중치 합계는 `1.0`이어야 한다.
- `riskThresholdHigh`는 `riskThresholdMedium`보다 커야 한다.
- 관리자 계정 생성은 이 API 범위에서 제외하고 학생/교수자 계정만 생성한다.
