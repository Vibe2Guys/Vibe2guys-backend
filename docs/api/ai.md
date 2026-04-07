# AI API

## Scope

- 꼬리질문 생성
- 꼬리질문 답변 저장
- 꼬리질문 분석 조회
- 개입 추천 생성

## Follow-up Question APIs

### `POST /api/v1/ai/follow-up-questions`

강의 내용, 퀴즈 답변, 과제 답변을 바탕으로 후속 질문을 생성한다.

Request:

```json
{
  "courseId": 101,
  "contentId": 5001,
  "studentId": 1,
  "contextType": "QUIZ",
  "sourceText": "AI는 데이터를 학습하여 문제를 해결한다."
}
```

Response:

```json
{
  "success": true,
  "message": "꼬리질문 생성 완료",
  "data": {
    "questionId": 9501,
    "questionText": "그렇다면 지도학습과 비지도학습은 어떤 차이가 있나요?",
    "difficultyLevel": "MEDIUM"
  }
}
```

### `POST /api/v1/ai/follow-up-questions/{questionId}/responses`

학생이 꼬리질문 답변을 제출한다.

### `GET /api/v1/ai/follow-up-questions/{questionId}/analysis`

꼬리질문 응답 분석 결과를 조회한다.

응답에는 최소한 아래 필드를 포함한다:

- `understandingScore`
- `feedback`
- `responseDelaySeconds`

## Internal AI Workflows

장기적으로는 아래 기능이 내부 워크플로우로 이동할 수 있다.

- short-answer analysis
- intervention recommendation generation
- concept-link summarization

## Backend Notes

- AI 출력은 항상 저장 시각과 입력 문맥을 함께 보관한다.
- 핵심 LMS 흐름은 AI 실패와 무관하게 정상 동작해야 한다.
- LLM 호출 결과는 사용자 응답에 바로 노출하기 전에 기본 검증과 후처리를 거친다.
