# Courses API

## Scope

- 내 강의 목록
- 전체 강의 목록
- 강의 생성/수정/상세
- 수강 신청
- 수강생 조회
- 주차 및 콘텐츠 관리
- 시청 진도 기록

## `GET /api/v1/courses/my`

학습자는 수강 중인 강의, 교수자는 담당 강의를 조회한다.

Response:

```json
{
  "success": true,
  "message": "내 강의 목록 조회 성공",
  "data": [
    {
      "courseId": 101,
      "title": "AI 기초",
      "description": "AI 개론 수업",
      "thumbnailUrl": "https://...",
      "instructorName": "김교수",
      "progressRate": 72,
      "attendanceRate": 85,
      "assignmentPendingCount": 2
    }
  ]
}
```

## `GET /api/v1/courses`

전체 강의 목록 조회.

Query Params:

- `page`
- `size`
- `keyword`

Response:

```json
{
  "success": true,
  "message": "강의 목록 조회 성공",
  "data": {
    "content": [
      {
        "courseId": 101,
        "title": "AI 기초",
        "instructorName": "김교수",
        "isEnrolled": true
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

## `POST /api/v1/courses`

교수자 또는 관리자가 강의를 생성한다.

Request:

```json
{
  "title": "AI 기초",
  "description": "AI 개론 수업입니다.",
  "thumbnailUrl": "https://...",
  "startDate": "2026-04-10",
  "endDate": "2026-06-30",
  "isSequentialRelease": true
}
```

## `GET /api/v1/courses/{courseId}`

강의 기본 정보, 주차 정보, 진행 상태를 포함한 상세를 조회한다.

## `PATCH /api/v1/courses/{courseId}`

강의 기본 정보를 수정한다.

## `POST /api/v1/courses/{courseId}/enrollments`

현재 사용자를 해당 강의에 수강 등록한다.

## `GET /api/v1/courses/{courseId}/students`

교수자가 수강생 목록을 조회한다.

Query Params:

- `page`
- `size`
- `keyword`

Response:

```json
{
  "success": true,
  "message": "수강생 목록 조회 성공",
  "data": {
    "content": [
      {
        "userId": 1,
        "name": "홍길동",
        "email": "hong@student.com",
        "status": "ENROLLED",
        "enrolledAt": "2026-04-10T09:00:00+09:00",
        "progressRate": 88,
        "attendanceRate": 92,
        "understandingScore": 84,
        "riskLevel": "LOW",
        "statusSummary": "안정",
        "memo": "발표 참여가 좋음"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

## `PATCH /api/v1/courses/{courseId}/students/{studentId}/memo`

교수자가 특정 수강생에 대한 메모를 저장한다.

Request:

```json
{
  "memo": "중간고사 전 보충 자료 안내 예정"
}
```

## Week / Content APIs

- `POST /api/v1/courses/{courseId}/weeks`
- `GET /api/v1/courses/{courseId}/weeks/{weekId}/contents`
- `POST /api/v1/weeks/{weekId}/contents`
- `GET /api/v1/contents/{contentId}`
- `POST /api/v1/uploads/presigned-url`

주차와 콘텐츠는 강의 모듈 하위 리소스로 관리한다. 콘텐츠 타입은 MVP에서 `VOD`, `LIVE`, `DOCUMENT`를 지원한다.
VOD / DOCUMENT 콘텐츠는 직접 파일을 받지 않고 presigned URL을 발급받아 S3에 업로드한 뒤, 반환된 `fileUrl`을 각각 `videoUrl`, `documentUrl`로 저장한다.

## Progress / Tracking APIs

- `POST /api/v1/contents/{contentId}/attendance`
- `PATCH /api/v1/contents/{contentId}/attendance`
- `POST /api/v1/contents/{contentId}/progress`
- `GET /api/v1/contents/{contentId}/progress`
- `GET /api/v1/courses/{courseId}/learning-logs/me`

VOD 진도 저장 예시:

```json
{
  "watchedSeconds": 620,
  "totalSeconds": 1800,
  "progressRate": 34,
  "lastPositionSeconds": 620,
  "replayCount": 2,
  "stoppedSegmentStart": 580,
  "stoppedSegmentEnd": 620,
  "eventType": "PAUSE"
}
```

## Backend Notes

- 출석 API의 `contentId`는 `type = LIVE`인 콘텐츠를 가리킨다.
- 시청 진도 API는 transactional progress 저장과 learning-event 적재를 함께 수행한다.
- Flutter 클라이언트는 5초에서 15초 단위 또는 `pause`/`ended` 시점에 진도 저장을 호출한다.
- 학생 뷰와 교수자 뷰 응답은 같은 엔드포인트라도 필드가 다를 수 있다.
