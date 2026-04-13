# Assignments API

## Scope

- 과제 조회/생성/제출
- 과제 제출 목록/채점
- 퀴즈 생성/조회/제출
- 퀴즈 결과 조회

## Assignment APIs

### `GET /api/v1/courses/{courseId}/assignments`

강의 과제 목록을 조회한다.

### `POST /api/v1/courses/{courseId}/assignments`

교수자가 개인 과제 또는 팀 과제를 생성한다.

Request:

```json
{
  "title": "1주차 요약 과제",
  "description": "강의 내용을 300자 이상 요약하세요.",
  "type": "SUBJECTIVE",
  "dueAt": "2026-04-15T23:59:59+09:00",
  "maxScore": 100,
  "teamAssignment": false
}
```

### `GET /api/v1/assignments/{assignmentId}`

과제 상세와 내 제출 상태를 조회한다. 응답에는 `maxScore`, `mySubmission.score`, `mySubmission.feedback`, `mySubmission.gradedAt`이 포함될 수 있다.

### `POST /api/v1/assignments/{assignmentId}/submissions`

개인 과제를 제출한다.

Request:

```json
{
  "answerText": "AI는 인간의 사고를 모방하는 기술이다...",
  "fileUrls": [
    "https://..."
  ]
}
```

### `PATCH /api/v1/assignments/{assignmentId}/submissions/{submissionId}`

과제를 재제출한다.

### `GET /api/v1/assignments/{assignmentId}/submissions`

교수자가 제출 목록을 조회한다. 각 항목에는 `score`, `feedback`, `gradedAt`이 포함될 수 있다.

### `PATCH /api/v1/assignments/{assignmentId}/submissions/{submissionId}/grade`

교수자가 제출물을 채점하고 피드백을 남긴다.

Request:

```json
{
  "score": 92,
  "feedback": "핵심 개념 정리가 정확합니다."
}
```

## Quiz APIs

### `POST /api/v1/courses/{courseId}/quizzes`

퀴즈와 문제 목록을 생성한다.

### `GET /api/v1/courses/{courseId}/quizzes`

강의 퀴즈 목록을 조회한다.

### `GET /api/v1/quizzes/{quizId}`

퀴즈 상세를 조회한다.

### `POST /api/v1/quizzes/{quizId}/submissions`

퀴즈 답안을 제출한다.

Request:

```json
{
  "answers": [
    {
      "questionId": 1,
      "selectedChoice": "B"
    },
    {
      "questionId": 2,
      "answerText": "AI는 의료 분야에서..."
    }
  ]
}
```

### `GET /api/v1/quizzes/{quizId}/results/me`

현재 사용자의 퀴즈 결과를 조회한다.

## Backend Notes

- 서술형 답변 분석은 제출 성공 이후 비동기로 처리할 수 있다.
- 팀 과제 여부는 응답에서 항상 명시한다.
- 객관식 점수는 동기 계산, 서술형 평가는 후처리 구조가 적합하다.
- 과제 점수는 `assignments.max_score`를 기준으로 저장하고, 학생 성적 화면에서는 백분율과 원점수를 함께 보여준다.
