좋아. 이번엔 진짜 프론트/백엔드가 같이 보면서 바로 개발 시작할 수 있는 형태로,
RESTful API 명세를 기능별로 정리해줄게.

너희 상황에서는 중요한 게 3가지다.
	1.	화면 기준으로 필요한 API가 빠지면 안 됨
	2.	프론트가 어떤 요청을 보내고 어떤 응답을 받는지 명확해야 함
	3.	처음부터 너무 복잡한 AI 모델 내부 구현은 숨기고, 결과만 주는 API로 가야 함

그래서 아래는 MVP 1단계 + 이후 확장 가능한 구조로 잡았다.
형식은 전부 다음처럼 맞출게.
	•	주소
	•	설명
	•	Request
	•	Response

⸻

0. 공통 규칙

Base URL

/api/v1

공통 응답 형식

성공

{
  "success": true,
  "message": "요청 성공",
  "data": {}
}

실패

{
  "success": false,
  "message": "에러 메시지",
  "errorCode": "COURSE_NOT_FOUND"
}

인증 방식

로그인 이후 모든 보호 API는 헤더에 JWT를 포함한다.

Authorization: Bearer {accessToken}

역할(Role)

STUDENT
INSTRUCTOR
ADMIN


⸻

1. 인증 / 사용자 API

⸻

1-1. 회원가입

주소

POST /api/v1/auth/register

설명

학습자 또는 교수자 계정을 생성한다.
MVP에서는 학습자만 공개 회원가입으로 받고, 교수자는 관리자 생성 방식으로 제한해도 된다.

Request

{
  "name": "홍길동",
  "email": "test@example.com",
  "password": "1234abcd!",
  "role": "STUDENT"
}

Response

{
  "success": true,
  "message": "회원가입 완료",
  "data": {
    "userId": 1,
    "name": "홍길동",
    "email": "test@example.com",
    "role": "STUDENT"
  }
}


⸻

1-2. 로그인

주소

POST /api/v1/auth/login

설명

이메일과 비밀번호로 로그인하고 access token을 발급한다.

Request

{
  "email": "test@example.com",
  "password": "1234abcd!"
}

Response

{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "accessToken": "jwt-token",
    "user": {
      "userId": 1,
      "name": "홍길동",
      "email": "test@example.com",
      "role": "STUDENT"
    }
  }
}


⸻

1-3. 내 정보 조회

주소

GET /api/v1/users/me

설명

현재 로그인한 사용자의 기본 정보를 조회한다.

Response

{
  "success": true,
  "message": "내 정보 조회 성공",
  "data": {
    "userId": 1,
    "name": "홍길동",
    "email": "test@example.com",
    "role": "STUDENT",
    "profileImageUrl": "https://..."
  }
}


⸻

1-4. 내 정보 수정

주소

PATCH /api/v1/users/me

Request

{
  "name": "홍길동2",
  "profileImageUrl": "https://..."
}

Response

{
  "success": true,
  "message": "내 정보 수정 완료",
  "data": {
    "userId": 1,
    "name": "홍길동2",
    "profileImageUrl": "https://..."
  }
}


⸻

2. 강의(Course) API

⸻

2-1. 내 강의 목록 조회

주소

GET /api/v1/courses/my

설명

학습자 기준 내가 수강 중인 강의 목록을 조회한다.
교수자라면 내가 담당하는 강의 목록을 내려줘도 된다.

Response

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


⸻

2-2. 전체 강의 목록 조회

주소

GET /api/v1/courses

설명

전체 강의 목록 조회.
관리자, 교수자, 또는 공개 강의 탐색용으로 사용.

Query Params

?page=0&size=10&keyword=AI

Response

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


⸻

2-3. 강의 생성

주소

POST /api/v1/courses

설명

교수자 또는 관리자가 강의를 생성한다.

Request

{
  "title": "AI 기초",
  "description": "AI 개론 수업입니다.",
  "thumbnailUrl": "https://...",
  "startDate": "2026-04-10",
  "endDate": "2026-06-30",
  "isSequentialRelease": true
}

Response

{
  "success": true,
  "message": "강의 생성 완료",
  "data": {
    "courseId": 101,
    "title": "AI 기초"
  }
}


⸻

2-4. 강의 상세 조회

주소

GET /api/v1/courses/{courseId}

설명

강의 기본 정보, 주차 정보, 진행 상태를 포함한 상세를 조회한다.

Response

{
  "success": true,
  "message": "강의 상세 조회 성공",
  "data": {
    "courseId": 101,
    "title": "AI 기초",
    "description": "AI 개론 수업입니다.",
    "instructor": {
      "userId": 20,
      "name": "김교수"
    },
    "startDate": "2026-04-10",
    "endDate": "2026-06-30",
    "weeks": [
      {
        "weekId": 1001,
        "weekNumber": 1,
        "title": "AI란 무엇인가",
        "isOpened": true
      }
    ]
  }
}


⸻

2-5. 강의 수정

주소

PATCH /api/v1/courses/{courseId}

Request

{
  "title": "AI 기초 입문",
  "description": "수정된 설명"
}

Response

{
  "success": true,
  "message": "강의 수정 완료",
  "data": {
    "courseId": 101,
    "title": "AI 기초 입문"
  }
}


⸻

2-6. 강의 수강 신청

주소

POST /api/v1/courses/{courseId}/enrollments

설명

현재 사용자를 해당 강의에 수강 등록한다.

Request

{}

Response

{
  "success": true,
  "message": "수강 신청 완료",
  "data": {
    "courseId": 101,
    "userId": 1,
    "status": "ENROLLED"
  }
}


⸻

2-7. 강의 수강생 목록 조회

주소

GET /api/v1/courses/{courseId}/students

설명

교수자가 수강생 목록을 조회한다.

Query Params

?page=0&size=20&keyword=홍

Response

{
  "success": true,
  "message": "수강생 목록 조회 성공",
  "data": {
    "content": [
      {
        "userId": 1,
        "name": "홍길동",
        "attendanceRate": 80,
        "progressRate": 65,
        "assignmentSubmitRate": 75,
        "riskLevel": "MEDIUM"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}


⸻

3. 주차(Week) / 콘텐츠(Content) API

⸻

3-1. 주차 생성

주소

POST /api/v1/courses/{courseId}/weeks

Request

{
  "weekNumber": 1,
  "title": "AI란 무엇인가",
  "openAt": "2026-04-10T09:00:00+09:00"
}

Response

{
  "success": true,
  "message": "주차 생성 완료",
  "data": {
    "weekId": 1001,
    "weekNumber": 1,
    "title": "AI란 무엇인가"
  }
}


⸻

3-2. 주차별 콘텐츠 목록 조회

주소

GET /api/v1/courses/{courseId}/weeks/{weekId}/contents

Response

{
  "success": true,
  "message": "주차별 콘텐츠 조회 성공",
  "data": [
    {
      "contentId": 5001,
      "type": "VOD",
      "title": "1주차 강의 영상",
      "durationSeconds": 1800,
      "isCompleted": false,
      "progressRate": 35
    },
    {
      "contentId": 5002,
      "type": "LIVE",
      "title": "1주차 실시간 강의",
      "scheduledAt": "2026-04-10T14:00:00+09:00"
    }
  ]
}


⸻

3-3. 콘텐츠 생성

주소

POST /api/v1/weeks/{weekId}/contents

설명

VOD, LIVE, DOCUMENT 등 콘텐츠를 생성한다.

Request

{
  "type": "VOD",
  "title": "1주차 강의 영상",
  "description": "OT 및 기본 개념",
  "videoUrl": "https://...",
  "durationSeconds": 1800,
  "openAt": "2026-04-10T09:00:00+09:00"
}

Response

{
  "success": true,
  "message": "콘텐츠 생성 완료",
  "data": {
    "contentId": 5001,
    "title": "1주차 강의 영상"
  }
}


⸻

3-4. 콘텐츠 상세 조회

주소

GET /api/v1/contents/{contentId}

Response

{
  "success": true,
  "message": "콘텐츠 상세 조회 성공",
  "data": {
    "contentId": 5001,
    "type": "VOD",
    "title": "1주차 강의 영상",
    "description": "OT 및 기본 개념",
    "videoUrl": "https://...",
    "durationSeconds": 1800,
    "myProgress": {
      "progressRate": 35,
      "lastPositionSeconds": 620,
      "isCompleted": false
    }
  }
}


⸻

4. 출석 / 시청 / 진도 추적 API

⸻

4-1. 라이브 강의 출석 시작

주소

POST /api/v1/live-sessions/{sessionId}/attendance

설명

학생이 실시간 강의에 입장했을 때 호출한다.

Request

{
  "enteredAt": "2026-04-10T14:01:10+09:00"
}

Response

{
  "success": true,
  "message": "출석 기록 완료",
  "data": {
    "attendanceId": 9001,
    "status": "PRESENT"
  }
}


⸻

4-2. 라이브 강의 출석 종료

주소

PATCH /api/v1/live-sessions/{sessionId}/attendance

Request

{
  "leftAt": "2026-04-10T15:10:00+09:00"
}

Response

{
  "success": true,
  "message": "퇴장 기록 완료",
  "data": {
    "attendanceId": 9001,
    "durationMinutes": 68
  }
}


⸻

4-3. VOD 진도 저장

주소

POST /api/v1/contents/{contentId}/progress

설명

영상 시청 중 현재 위치와 진도율을 저장한다.
프론트는 5~15초 단위 또는 pause/ended 이벤트에서 호출하면 된다.

Request

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

Response

{
  "success": true,
  "message": "진도 저장 완료",
  "data": {
    "contentId": 5001,
    "progressRate": 34,
    "isCompleted": false
  }
}


⸻

4-4. 내 콘텐츠 진도 조회

주소

GET /api/v1/contents/{contentId}/progress

Response

{
  "success": true,
  "message": "진도 조회 성공",
  "data": {
    "contentId": 5001,
    "progressRate": 34,
    "lastPositionSeconds": 620,
    "replayCount": 2,
    "isCompleted": false
  }
}


⸻

4-5. 강의 전체 학습 로그 조회

주소

GET /api/v1/courses/{courseId}/learning-logs/me

설명

학습자 본인의 강의 내 전체 학습 로그 요약을 조회한다.

Response

{
  "success": true,
  "message": "학습 로그 조회 성공",
  "data": {
    "attendanceRate": 85,
    "progressRate": 72,
    "averageWatchTimeMinutes": 43,
    "replayHotspots": [
      {
        "contentId": 5001,
        "segmentStart": 300,
        "segmentEnd": 420,
        "repeatCount": 4
      }
    ]
  }
}


⸻

5. 과제(Assignment) API

⸻

5-1. 강의 과제 목록 조회

주소

GET /api/v1/courses/{courseId}/assignments

Response

{
  "success": true,
  "message": "과제 목록 조회 성공",
  "data": [
    {
      "assignmentId": 7001,
      "title": "1주차 요약 과제",
      "type": "SUBJECTIVE",
      "dueAt": "2026-04-15T23:59:59+09:00",
      "isSubmitted": false,
      "teamAssignment": false
    }
  ]
}


⸻

5-2. 과제 생성

주소

POST /api/v1/courses/{courseId}/assignments

설명

교수자가 개인 과제 또는 팀 과제를 생성한다.

Request

{
  "title": "1주차 요약 과제",
  "description": "강의 내용을 300자 이상 요약하세요.",
  "type": "SUBJECTIVE",
  "dueAt": "2026-04-15T23:59:59+09:00",
  "teamAssignment": false
}

Response

{
  "success": true,
  "message": "과제 생성 완료",
  "data": {
    "assignmentId": 7001,
    "title": "1주차 요약 과제"
  }
}


⸻

5-3. 과제 상세 조회

주소

GET /api/v1/assignments/{assignmentId}

Response

{
  "success": true,
  "message": "과제 상세 조회 성공",
  "data": {
    "assignmentId": 7001,
    "title": "1주차 요약 과제",
    "description": "강의 내용을 300자 이상 요약하세요.",
    "type": "SUBJECTIVE",
    "dueAt": "2026-04-15T23:59:59+09:00",
    "teamAssignment": false,
    "mySubmission": {
      "submissionId": null,
      "status": "NOT_SUBMITTED"
    }
  }
}


⸻

5-4. 과제 제출

주소

POST /api/v1/assignments/{assignmentId}/submissions

설명

개인 과제를 제출한다.

Request

{
  "answerText": "AI는 인간의 사고를 모방하는 기술이다...",
  "fileUrls": [
    "https://..."
  ]
}

Response

{
  "success": true,
  "message": "과제 제출 완료",
  "data": {
    "submissionId": 8001,
    "submittedAt": "2026-04-14T22:10:00+09:00",
    "status": "SUBMITTED"
  }
}


⸻

5-5. 과제 재제출

주소

PATCH /api/v1/assignments/{assignmentId}/submissions/{submissionId}

Request

{
  "answerText": "수정된 답변입니다."
}

Response

{
  "success": true,
  "message": "과제 재제출 완료",
  "data": {
    "submissionId": 8001,
    "status": "RESUBMITTED"
  }
}


⸻

5-6. 과제 제출 목록 조회

주소

GET /api/v1/assignments/{assignmentId}/submissions

설명

교수자가 해당 과제의 제출자 목록을 조회한다.

Response

{
  "success": true,
  "message": "과제 제출 목록 조회 성공",
  "data": [
    {
      "submissionId": 8001,
      "studentId": 1,
      "studentName": "홍길동",
      "submittedAt": "2026-04-14T22:10:00+09:00",
      "status": "SUBMITTED"
    }
  ]
}


⸻

6. 퀴즈(Quiz) API

⸻

6-1. 퀴즈 생성

주소

POST /api/v1/courses/{courseId}/quizzes

Request

{
  "title": "1주차 퀴즈",
  "dueAt": "2026-04-16T23:59:59+09:00",
  "questions": [
    {
      "questionType": "MULTIPLE_CHOICE",
      "questionText": "AI의 정의로 가장 적절한 것은?",
      "choices": ["A", "B", "C", "D"],
      "answer": "B",
      "score": 10
    },
    {
      "questionType": "SUBJECTIVE",
      "questionText": "AI의 활용 사례를 서술하세요.",
      "score": 20
    }
  ]
}

Response

{
  "success": true,
  "message": "퀴즈 생성 완료",
  "data": {
    "quizId": 6001,
    "title": "1주차 퀴즈"
  }
}


⸻

6-2. 퀴즈 목록 조회

주소

GET /api/v1/courses/{courseId}/quizzes

Response

{
  "success": true,
  "message": "퀴즈 목록 조회 성공",
  "data": [
    {
      "quizId": 6001,
      "title": "1주차 퀴즈",
      "dueAt": "2026-04-16T23:59:59+09:00",
      "isSubmitted": false
    }
  ]
}


⸻

6-3. 퀴즈 상세 조회

주소

GET /api/v1/quizzes/{quizId}

Response

{
  "success": true,
  "message": "퀴즈 상세 조회 성공",
  "data": {
    "quizId": 6001,
    "title": "1주차 퀴즈",
    "dueAt": "2026-04-16T23:59:59+09:00",
    "questions": [
      {
        "questionId": 1,
        "questionType": "MULTIPLE_CHOICE",
        "questionText": "AI의 정의로 가장 적절한 것은?",
        "choices": ["A", "B", "C", "D"],
        "score": 10
      }
    ]
  }
}


⸻

6-4. 퀴즈 제출

주소

POST /api/v1/quizzes/{quizId}/submissions

Request

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

Response

{
  "success": true,
  "message": "퀴즈 제출 완료",
  "data": {
    "quizSubmissionId": 6101,
    "objectiveScore": 10,
    "subjectivePending": true,
    "status": "SUBMITTED"
  }
}


⸻

6-5. 내 퀴즈 결과 조회

주소

GET /api/v1/quizzes/{quizId}/results/me

Response

{
  "success": true,
  "message": "퀴즈 결과 조회 성공",
  "data": {
    "quizId": 6001,
    "totalScore": 25,
    "objectiveScore": 10,
    "subjectiveScore": 15,
    "feedback": "핵심 용어는 이해했으나 적용 설명이 부족합니다."
  }
}


⸻

7. AI 꼬리질문 API

⸻

7-1. 꼬리질문 생성

주소

POST /api/v1/ai/follow-up-questions

설명

강의 내용, 퀴즈 답변, 과제 답변을 바탕으로 후속 질문을 생성한다.

Request

{
  "courseId": 101,
  "contentId": 5001,
  "studentId": 1,
  "contextType": "QUIZ",
  "sourceText": "AI는 데이터를 학습하여 문제를 해결한다."
}

Response

{
  "success": true,
  "message": "꼬리질문 생성 완료",
  "data": {
    "questionId": 9501,
    "questionText": "그렇다면 지도학습과 비지도학습은 어떤 차이가 있나요?",
    "difficultyLevel": "MEDIUM"
  }
}


⸻

7-2. 꼬리질문 답변 제출

주소

POST /api/v1/ai/follow-up-questions/{questionId}/responses

Request

{
  "answerText": "지도학습은 정답이 있는 데이터를 사용하고..."
}

Response

{
  "success": true,
  "message": "꼬리질문 답변 제출 완료",
  "data": {
    "responseId": 9601,
    "submittedAt": "2026-04-06T22:00:00+09:00"
  }
}


⸻

7-3. 꼬리질문 응답 분석 조회

주소

GET /api/v1/ai/follow-up-questions/{questionId}/analysis

Response

{
  "success": true,
  "message": "꼬리질문 분석 조회 성공",
  "data": {
    "questionId": 9501,
    "understandingScore": 72,
    "feedback": "개념 정의는 맞지만 두 학습 방식의 차이를 사례와 연결하는 부분이 부족합니다.",
    "responseDelaySeconds": 48
  }
}


⸻

8. AI 이해도 / 이탈 위험 / 추천 API

⸻

8-1. 학생 AI 이해도 분석 조회

주소

GET /api/v1/students/{studentId}/ai-understanding

설명

교수자용 학생 개별 이해도 분석 결과 조회

Response

{
  "success": true,
  "message": "이해도 분석 조회 성공",
  "data": {
    "studentId": 1,
    "understandingScore": 68,
    "summary": "개념 1과 2는 이해했지만 개념 2와 3의 연결이 부족합니다.",
    "missingConcepts": ["모델 일반화", "과적합"],
    "misconceptions": ["지도학습과 강화학습 혼동"],
    "conceptLinks": [
      {
        "from": "데이터",
        "to": "학습",
        "strength": 0.8
      },
      {
        "from": "학습",
        "to": "일반화",
        "strength": 0.3
      }
    ]
  }
}


⸻

8-2. 학생 이탈 위험 조회

주소

GET /api/v1/students/{studentId}/risk

Response

{
  "success": true,
  "message": "이탈 위험 조회 성공",
  "data": {
    "studentId": 1,
    "riskScore": 78,
    "riskLevel": "HIGH",
    "reasons": [
      "최근 2주간 시청률 감소",
      "과제 미제출 2회",
      "꼬리질문 응답 지연 증가"
    ]
  }
}


⸻

8-3. 학생 맞춤 추천 조회

주소

GET /api/v1/students/{studentId}/recommendations

설명

학습자용 개인 맞춤 추천

Response

{
  "success": true,
  "message": "추천 조회 성공",
  "data": {
    "reviewConcepts": ["지도학습", "과적합"],
    "recommendedContents": [
      {
        "contentId": 5001,
        "title": "1주차 강의 영상",
        "reason": "반복 시청 구간이 많았던 개념 포함"
      }
    ],
    "recommendedActions": [
      "오늘 10분 복습하기",
      "꼬리질문 1개 더 풀어보기",
      "팀 토론에 의견 1회 남기기"
    ]
  }
}


⸻

8-4. 교수자용 개입 추천 조회

주소

GET /api/v1/instructors/courses/{courseId}/interventions

Response

{
  "success": true,
  "message": "개입 추천 조회 성공",
  "data": [
    {
      "studentId": 1,
      "studentName": "홍길동",
      "recommendedAction": "상담 권장",
      "reason": "과제 미제출 증가 + 팀 활동 저조"
    },
    {
      "studentId": 3,
      "studentName": "김학생",
      "recommendedAction": "보충자료 발송",
      "reason": "오개념 반복 탐지"
    }
  ]
}


⸻

9. 학습자 대시보드 API

⸻

9-1. 학습자 메인 대시보드 조회

주소

GET /api/v1/dashboard/student

Response

{
  "success": true,
  "message": "학습자 대시보드 조회 성공",
  "data": {
    "attendanceRate": 85,
    "progressRate": 72,
    "assignmentSubmitRate": 75,
    "understandingScore": 68,
    "engagementScore": 70,
    "riskLevel": "MEDIUM",
    "coachingMessage": "최근 학습 흐름이 조금 떨어지고 있어요. 1주차 핵심 개념을 다시 복습해보세요.",
    "todayTodos": [
      "1주차 복습",
      "퀴즈 1개 응시",
      "팀 채팅 참여"
    ]
  }
}


⸻

9-2. 내 학습 리포트 조회

주소

GET /api/v1/reports/me

Response

{
  "success": true,
  "message": "내 학습 리포트 조회 성공",
  "data": {
    "weeklySummary": "이번 주는 출석은 안정적이지만, 특정 개념에서 반복 시청이 많았습니다.",
    "strengths": ["출석 성실", "퀴즈 점수 양호"],
    "weaknesses": ["개념 연결 부족", "팀 토론 참여 저조"],
    "nextActions": [
      "과적합 개념 복습",
      "꼬리질문 답변하기"
    ]
  }
}


⸻

10. 교수자 대시보드 API

⸻

10-1. 교수자 메인 대시보드 조회

주소

GET /api/v1/dashboard/instructor/courses/{courseId}

Response

{
  "success": true,
  "message": "교수자 대시보드 조회 성공",
  "data": {
    "courseId": 101,
    "courseTitle": "AI 기초",
    "studentCount": 40,
    "averageAttendanceRate": 82,
    "averageProgressRate": 70,
    "highRiskStudentCount": 5,
    "lowUnderstandingStudentCount": 7,
    "teamAlertCount": 2,
    "bottleneckConcepts": [
      "과적합",
      "일반화"
    ]
  }
}


⸻

10-2. 위험 학생 목록 조회

주소

GET /api/v1/instructors/courses/{courseId}/students/risk

Response

{
  "success": true,
  "message": "위험 학생 목록 조회 성공",
  "data": [
    {
      "studentId": 1,
      "studentName": "홍길동",
      "riskScore": 78,
      "riskLevel": "HIGH",
      "reasons": ["과제 미제출", "응답 지연"]
    }
  ]
}


⸻

10-3. 이해도 낮은 학생 목록 조회

주소

GET /api/v1/instructors/courses/{courseId}/students/understanding-low

Response

{
  "success": true,
  "message": "이해도 낮은 학생 조회 성공",
  "data": [
    {
      "studentId": 2,
      "studentName": "김학생",
      "understandingScore": 52,
      "missingConcepts": ["지도학습", "손실함수"]
    }
  ]
}


⸻

10-4. 학생 상세 분석 조회

주소

GET /api/v1/instructors/courses/{courseId}/students/{studentId}

Response

{
  "success": true,
  "message": "학생 상세 분석 조회 성공",
  "data": {
    "studentId": 1,
    "studentName": "홍길동",
    "attendanceRate": 85,
    "progressRate": 72,
    "assignmentSubmitRate": 75,
    "quizAverageScore": 68,
    "understandingScore": 68,
    "engagementScore": 70,
    "riskScore": 78,
    "teamContributionScore": 40,
    "followUpResponsePattern": {
      "averageDelaySeconds": 50,
      "responseCount": 8
    },
    "recommendedIntervention": "상담 권장"
  }
}


⸻

11. 팀(모둠) API

⸻

11-1. 팀 자동 생성

주소

POST /api/v1/courses/{courseId}/teams/auto-grouping

설명

수강생을 자동으로 팀 배정한다.

Request

{
  "teamSize": 4
}

Response

{
  "success": true,
  "message": "팀 자동 배정 완료",
  "data": {
    "teamCount": 10
  }
}


⸻

11-2. 팀 목록 조회

주소

GET /api/v1/courses/{courseId}/teams

Response

{
  "success": true,
  "message": "팀 목록 조회 성공",
  "data": [
    {
      "teamId": 3001,
      "teamName": "1팀",
      "memberCount": 4,
      "collaborationScore": 72
    }
  ]
}


⸻

11-3. 내 팀 조회

주소

GET /api/v1/teams/me

Response

{
  "success": true,
  "message": "내 팀 조회 성공",
  "data": {
    "teamId": 3001,
    "teamName": "1팀",
    "courseId": 101,
    "members": [
      {
        "userId": 1,
        "name": "홍길동"
      },
      {
        "userId": 2,
        "name": "김학생"
      }
    ]
  }
}


⸻

11-4. 팀 상세 조회

주소

GET /api/v1/teams/{teamId}

Response

{
  "success": true,
  "message": "팀 상세 조회 성공",
  "data": {
    "teamId": 3001,
    "teamName": "1팀",
    "members": [
      {
        "userId": 1,
        "name": "홍길동",
        "contributionScore": 50
      }
    ],
    "collaborationScore": 72,
    "riskSignals": ["특정 학생 채팅 참여 없음"]
  }
}


⸻

11-5. 팀 재배치

주소

PATCH /api/v1/teams/{teamId}/members

설명

교수자가 팀원을 재배치한다.

Request

{
  "removeMemberIds": [1],
  "addMemberIds": [5]
}

Response

{
  "success": true,
  "message": "팀원 수정 완료",
  "data": {
    "teamId": 3001
  }
}


⸻

12. 팀 채팅 / 토론 API

실시간 채팅은 보통 REST + WebSocket 조합으로 간다.
채팅방 생성/조회는 REST, 실제 메시지 송수신은 WebSocket이 더 맞다.

⸻

12-1. 팀 채팅방 조회

주소

GET /api/v1/teams/{teamId}/chat-room

Response

{
  "success": true,
  "message": "채팅방 조회 성공",
  "data": {
    "chatRoomId": 4001,
    "teamId": 3001
  }
}


⸻

12-2. 채팅 메시지 목록 조회

주소

GET /api/v1/chat-rooms/{chatRoomId}/messages

Query Params

?cursor=100&size=20

Response

{
  "success": true,
  "message": "채팅 메시지 조회 성공",
  "data": {
    "messages": [
      {
        "messageId": 101,
        "senderId": 1,
        "senderName": "홍길동",
        "message": "이 개념은 이렇게 이해하면 될까요?",
        "sentAt": "2026-04-06T21:00:00+09:00"
      }
    ],
    "nextCursor": 80
  }
}


⸻

12-3. 채팅 메시지 저장

주소

POST /api/v1/chat-rooms/{chatRoomId}/messages

설명

WebSocket 못 쓸 경우 임시 REST 방식으로도 저장 가능.

Request

{
  "message": "저는 과적합을 이렇게 이해했어요."
}

Response

{
  "success": true,
  "message": "메시지 전송 완료",
  "data": {
    "messageId": 102
  }
}


⸻

13. 협업 분석 API

⸻

13-1. 팀 협업 분석 조회

주소

GET /api/v1/teams/{teamId}/analytics

Response

{
  "success": true,
  "message": "팀 협업 분석 조회 성공",
  "data": {
    "teamId": 3001,
    "collaborationScore": 72,
    "conversationBalanceScore": 60,
    "inactiveMemberCount": 1,
    "dominantMemberCount": 1,
    "riskSignals": [
      "특정 학생 참여율 낮음",
      "응답 편중 현상"
    ]
  }
}


⸻

13-2. 개인 팀 기여도 조회

주소

GET /api/v1/teams/{teamId}/members/contributions

Response

{
  "success": true,
  "message": "개인 기여도 조회 성공",
  "data": [
    {
      "userId": 1,
      "name": "홍길동",
      "messageCount": 12,
      "feedbackCount": 3,
      "taskContributionScore": 65,
      "contributionScore": 58
    }
  ]
}


⸻

14. 교수자 개입 API

⸻

14-1. 상담 요청 보내기

주소

POST /api/v1/instructors/students/{studentId}/counseling-requests

Request

{
  "courseId": 101,
  "message": "최근 학습 흐름 점검을 위해 상담을 요청합니다."
}

Response

{
  "success": true,
  "message": "상담 요청 전송 완료",
  "data": {
    "requestId": 1101
  }
}


⸻

14-2. 보충자료 발송

주소

POST /api/v1/instructors/students/{studentId}/supplement-materials

Request

{
  "courseId": 101,
  "title": "과적합 복습 자료",
  "content": "이 자료를 참고해 복습해보세요.",
  "resourceUrls": [
    "https://..."
  ]
}

Response

{
  "success": true,
  "message": "보충자료 발송 완료",
  "data": {
    "sent": true
  }
}


⸻

14-3. 추가 과제 부여

주소

POST /api/v1/instructors/students/{studentId}/extra-assignments

Request

{
  "courseId": 101,
  "title": "추가 복습 과제",
  "description": "과적합과 일반화의 차이를 정리하세요.",
  "dueAt": "2026-04-20T23:59:59+09:00"
}

Response

{
  "success": true,
  "message": "추가 과제 부여 완료",
  "data": {
    "assignmentId": 7201
  }
}


⸻

15. 관리자 API

⸻

15-1. 사용자 목록 조회

주소

GET /api/v1/admin/users

Query Params

?page=0&size=20&role=STUDENT&keyword=홍

Response

{
  "success": true,
  "message": "사용자 목록 조회 성공",
  "data": {
    "content": [
      {
        "userId": 1,
        "name": "홍길동",
        "email": "test@example.com",
        "role": "STUDENT",
        "status": "ACTIVE"
      }
    ]
  }
}


⸻

15-2. 교수자/학생 계정 생성

주소

POST /api/v1/admin/users

Request

{
  "name": "김교수",
  "email": "prof@example.com",
  "password": "1234abcd!",
  "role": "INSTRUCTOR"
}

Response

{
  "success": true,
  "message": "계정 생성 완료",
  "data": {
    "userId": 20
  }
}


⸻

15-3. AI 분석 기준 설정 조회

주소

GET /api/v1/admin/analytics-config

Response

{
  "success": true,
  "message": "분석 기준 조회 성공",
  "data": {
    "attendanceWeight": 0.2,
    "progressWeight": 0.2,
    "assignmentWeight": 0.2,
    "quizWeight": 0.2,
    "teamActivityWeight": 0.2,
    "riskThresholdHigh": 75,
    "riskThresholdMedium": 50
  }
}


⸻

15-4. AI 분석 기준 수정

주소

PATCH /api/v1/admin/analytics-config

Request

{
  "attendanceWeight": 0.15,
  "progressWeight": 0.25,
  "assignmentWeight": 0.2,
  "quizWeight": 0.25,
  "teamActivityWeight": 0.15,
  "riskThresholdHigh": 80,
  "riskThresholdMedium": 55
}

Response

{
  "success": true,
  "message": "분석 기준 수정 완료",
  "data": {
    "updated": true
  }
}


⸻

16. 지표 / 점수 API

이건 프론트에서 대시보드 그릴 때 핵심이다.

⸻

16-1. 학생 종합 점수 조회

주소

GET /api/v1/students/{studentId}/scores

Response

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


⸻

16-2. 강의 전체 분포 조회

주소

GET /api/v1/instructors/courses/{courseId}/score-distribution

Response

{
  "success": true,
  "message": "점수 분포 조회 성공",
  "data": {
    "understanding": {
      "high": 10,
      "medium": 20,
      "low": 10
    },
    "risk": {
      "high": 5,
      "medium": 8,
      "low": 27
    }
  }
}


⸻

17. 알림 API

⸻

17-1. 내 알림 목록 조회

주소

GET /api/v1/notifications/me

Response

{
  "success": true,
  "message": "알림 조회 성공",
  "data": [
    {
      "notificationId": 1,
      "type": "ASSIGNMENT_DUE",
      "title": "과제 마감 임박",
      "content": "1주차 요약 과제가 내일 마감입니다.",
      "isRead": false,
      "createdAt": "2026-04-06T20:00:00+09:00"
    }
  ]
}


⸻

17-2. 알림 읽음 처리

주소

PATCH /api/v1/notifications/{notificationId}/read

Response

{
  "success": true,
  "message": "알림 읽음 처리 완료",
  "data": {
    "notificationId": 1,
    "isRead": true
  }
}


⸻

18. 프론트 화면 기준 추천 매핑

이건 진짜 중요하다. 프론트가 어떤 화면에서 어떤 API를 부르면 되는지까지 정리해야 안 꼬인다.

학습자

로그인 페이지
	•	POST /api/v1/auth/login

내 강의 목록 페이지
	•	GET /api/v1/courses/my

강의 상세 페이지
	•	GET /api/v1/courses/{courseId}
	•	GET /api/v1/courses/{courseId}/assignments
	•	GET /api/v1/courses/{courseId}/quizzes

콘텐츠 시청 페이지
	•	GET /api/v1/contents/{contentId}
	•	POST /api/v1/contents/{contentId}/progress
	•	POST /api/v1/ai/follow-up-questions
	•	POST /api/v1/ai/follow-up-questions/{questionId}/responses

과제 페이지
	•	GET /api/v1/assignments/{assignmentId}
	•	POST /api/v1/assignments/{assignmentId}/submissions

퀴즈 페이지
	•	GET /api/v1/quizzes/{quizId}
	•	POST /api/v1/quizzes/{quizId}/submissions

내 리포트 페이지
	•	GET /api/v1/dashboard/student
	•	GET /api/v1/reports/me
	•	GET /api/v1/students/{studentId}/recommendations

팀 활동 페이지
	•	GET /api/v1/teams/me
	•	GET /api/v1/teams/{teamId}
	•	GET /api/v1/teams/{teamId}/chat-room
	•	GET /api/v1/chat-rooms/{chatRoomId}/messages

⸻

교수자

교수자 메인 대시보드
	•	GET /api/v1/dashboard/instructor/courses/{courseId}
	•	GET /api/v1/instructors/courses/{courseId}/students/risk
	•	GET /api/v1/instructors/courses/{courseId}/students/understanding-low
	•	GET /api/v1/instructors/courses/{courseId}/interventions

강의 관리 페이지
	•	POST /api/v1/courses
	•	PATCH /api/v1/courses/{courseId}
	•	POST /api/v1/courses/{courseId}/assignments
	•	POST /api/v1/courses/{courseId}/quizzes

학생 리스트 / 상세 페이지
	•	GET /api/v1/courses/{courseId}/students
	•	GET /api/v1/instructors/courses/{courseId}/students/{studentId}
	•	GET /api/v1/students/{studentId}/ai-understanding
	•	GET /api/v1/students/{studentId}/risk

팀 분석 페이지
	•	GET /api/v1/courses/{courseId}/teams
	•	GET /api/v1/teams/{teamId}/analytics
	•	GET /api/v1/teams/{teamId}/members/contributions

⸻

19. MVP에서 진짜 먼저 만들 순서

너희가 둘이서 바이브 코딩이면, 이걸 한 번에 다 만들면 망한다.
그래서 개발 순서도 같이 잡아야 한다.

1순위
	•	인증
	•	내 강의 목록
	•	강의 상세
	•	콘텐츠 조회
	•	진도 저장
	•	과제 목록/제출
	•	교수자 대시보드 기본 조회

2순위
	•	퀴즈
	•	학생별 점수 조회
	•	위험 학생 조회
	•	팀 생성 / 팀 조회 / 채팅 조회

3순위
	•	AI 꼬리질문
	•	이해도 분석
	•	협업 분석
	•	개입 추천

⸻

20. 추천 DB 리소스 구조

RESTful 하게 가려면 대략 리소스를 이렇게 보면 된다.
	•	users
	•	auth
	•	courses
	•	weeks
	•	contents
	•	live-sessions
	•	assignments
	•	submissions
	•	quizzes
	•	teams
	•	chat-rooms
	•	messages
	•	notifications
	•	reports
	•	dashboard
	•	analytics
	•	recommendations

즉, “행동”보다는 리소스 중심으로 주소를 잡고,
정말 필요한 경우만 auto-grouping, read, enrollments 같은 보조 액션을 붙이면 된다.

⸻

원하면 다음 답변에서 바로 이어서
이 API 명세를 기반으로 ERD 엔티티까지 뽑아줄게.
예를 들면 User / Course / Enrollment / Week / Content / Progress / Assignment / Submission / Quiz / Team / ChatMessage 이렇게 바로 개발 가능한 수준으로.