# Analytics API

## Scope

- 학생 점수 조회
- AI 이해도/위험도/추천 조회
- 학습자 대시보드
- 교수자 대시보드
- 점수 분포
- 알림

## Response Principles

- 점수와 근거를 같이 반환한다.
- 시간 범위 또는 계산 기준 시점을 포함한다.
- 프론트 표시용 요약과 원시 근거는 필요시 분리한다.

## Student Analytics APIs

### `GET /api/v1/students/{studentId}/scores`

학생 종합 점수를 조회한다.

Response:

```json
{
  "success": true,
  "message": "학생 점수 조회 성공",
  "data": {
    "learningSincerityScore": 80,
    "understandingScore": 68,
    "engagementScore": 70,
    "collaborationScore": 58,
    "riskScore": 78
  }
}
```

### `GET /api/v1/students/{studentId}/ai-understanding`

교수자용 학생 개별 이해도 분석 결과를 조회한다.

응답에는 규칙 기반 이해도 점수, 강점/공백 요약, 최근 꼬리질문 분석 결과를 포함한다.

### `GET /api/v1/students/{studentId}/risk`

학생의 이탈 위험 점수와 이유를 조회한다.

### `GET /api/v1/students/{studentId}/recommendations`

학습자용 맞춤 추천을 조회한다.

## Student Dashboard APIs

- `GET /api/v1/dashboard/student`
- `GET /api/v1/reports/me`

학습자 대시보드 응답에는 아래 항목을 우선 포함한다:

- `attendanceRate`
- `progressRate`
- `assignmentSubmitRate`
- `understandingScore`
- `engagementScore`
- `riskLevel`
- `coachingMessage`
- `todayTodos`

## Instructor Dashboard APIs

- `GET /api/v1/dashboard/instructor/courses/{courseId}`
- `GET /api/v1/instructors/courses/{courseId}/students/risk`
- `GET /api/v1/instructors/courses/{courseId}/students/understanding-low`
- `GET /api/v1/instructors/courses/{courseId}/students/{studentId}`
- `GET /api/v1/instructors/courses/{courseId}/interventions`
- `POST /api/v1/instructors/courses/{courseId}/interventions`
- `GET /api/v1/instructors/courses/{courseId}/score-distribution`

교수자 대시보드는 평균 진도, 고위험 학생 수, 병목 개념, 팀 경고 수를 바로 그릴 수 있어야 한다.

`POST /api/v1/instructors/courses/{courseId}/interventions` 요청 예시:

```json
{
  "studentId": 12,
  "type": "COUNSELING",
  "title": "1:1 상담 제안",
  "message": "최근 2주간 진도율과 응답 속도가 떨어져 상담을 권장합니다.",
  "resourceUrls": [
    "https://example.com/review-note"
  ]
}
```

## Notifications APIs

- `GET /api/v1/notifications/me`
- `PATCH /api/v1/notifications/{notificationId}/read`

알림은 MVP에서 아래 이벤트를 우선 지원한다:

- 새 콘텐츠 공개 또는 등록
- 새 과제 등록
- AI 꼬리질문 도착

## Backend Notes

- 규칙 기반 계산 결과를 우선 제공하고 AI 요약은 보조 필드로 붙인다.
- `risk` 응답은 반드시 `reasons` 배열을 포함한다.
- 점수 계산 로직은 버전 관리가 가능해야 한다.
