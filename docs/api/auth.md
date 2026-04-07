# Auth API

## Common Rules

- Base URL: `/api/v1`
- Auth header: `Authorization: Bearer {accessToken}`
- Roles: `STUDENT`, `INSTRUCTOR`, `ADMIN`

Success response:

```json
{
  "success": true,
  "message": "요청 성공",
  "data": {}
}
```

Error response:

```json
{
  "success": false,
  "message": "에러 메시지",
  "errorCode": "AUTH_REQUIRED"
}
```

## Scope

- 회원가입
- 로그인
- 토큰 재발급
- 내 정보 조회
- 내 정보 수정

## `POST /api/v1/auth/register`

학습자 또는 교수자 계정을 생성한다. MVP에서는 학습자 공개 회원가입, 교수자 계정은 관리자 생성으로 제한할 수 있다.

Request:

```json
{
  "name": "홍길동",
  "email": "test@example.com",
  "password": "1234abcd!",
  "role": "STUDENT"
}
```

Response:

```json
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
```

## `POST /api/v1/auth/login`

이메일과 비밀번호로 로그인하고 액세스 토큰을 발급한다.

Request:

```json
{
  "email": "test@example.com",
  "password": "1234abcd!"
}
```

## `POST /api/v1/auth/refresh`

refresh token으로 access token을 재발급한다.

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```json
{
  "success": true,
  "message": "토큰 재발급 성공",
  "data": {
    "accessToken": "new-jwt-token",
    "refreshToken": "rotated-refresh-token"
  }
}
```

Response:

```json
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
```

## `GET /api/v1/users/me`

현재 로그인한 사용자의 기본 정보를 조회한다.

Response:

```json
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
```

## `PATCH /api/v1/users/me`

현재 로그인한 사용자의 프로필 정보를 수정한다.

Request:

```json
{
  "name": "홍길동2",
  "profileImageUrl": "https://..."
}
```

Response:

```json
{
  "success": true,
  "message": "내 정보 수정 완료",
  "data": {
    "userId": 1,
    "name": "홍길동2",
    "profileImageUrl": "https://..."
  }
}
```

## Backend Notes

- Spring Security + JWT 기준으로 구현한다.
- Flutter 앱에서 토큰 재사용이 쉬워야 하므로 응답 구조를 단순하게 유지한다.
- Refresh token은 MVP 범위에 포함하며 DB 저장과 회전을 지원한다.
