# Teams API

## Scope

- 팀 자동 생성
- 팀 조회 및 재배치
- 팀 채팅
- 협업 분석
- 교수자 개입 액션

## Team APIs

### `POST /api/v1/courses/{courseId}/teams/auto-grouping`

수강생을 자동으로 팀 배정한다.

### `GET /api/v1/courses/{courseId}/teams`

강의 내 팀 목록을 조회한다.

### `GET /api/v1/teams/me`

현재 사용자의 팀 정보를 조회한다.

### `GET /api/v1/teams/{teamId}`

팀 상세, 팀원 기여도, 위험 신호를 조회한다.

### `PATCH /api/v1/teams/{teamId}/members`

교수자가 팀원을 재배치한다.

Request:

```json
{
  "removeMemberIds": [1],
  "addMemberIds": [5]
}
```

## Team Chat APIs

- `GET /api/v1/teams/{teamId}/chat-room`
- `GET /api/v1/chat-rooms/{chatRoomId}/messages`
- `POST /api/v1/chat-rooms/{chatRoomId}/messages`

MVP에서는 REST 기반 저장으로 시작할 수 있고, 이후 WebSocket 전송을 추가한다.

## Collaboration Analytics APIs

- `GET /api/v1/teams/{teamId}/analytics`
- `GET /api/v1/teams/{teamId}/members/contributions`

팀 협업 분석 응답에는 최소한 아래 정보가 포함된다:

- `collaborationScore`
- `conversationBalanceScore`
- `inactiveMemberCount`
- `dominantMemberCount`
- `riskSignals`

## Instructor Intervention APIs

- `GET /api/v1/instructors/courses/{courseId}/interventions`
- `POST /api/v1/instructors/courses/{courseId}/interventions`

교수자 개입은 팀 모듈 전용 엔드포인트로 분리하지 않고, 코스 기준 개입 기록 API로 통합한다.
개입 타입은 `COUNSELING`, `SUPPLEMENT_MATERIAL`, `EXTRA_ASSIGNMENT`를 사용한다.

## Backend Notes

- 채팅은 저장과 실시간 전송을 분리해 설계한다.
- 팀 재배치 이력은 남겨야 한다.
- 팀 활동 이벤트는 협업 점수 계산의 입력으로 직접 사용한다.
