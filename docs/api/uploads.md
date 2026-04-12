# Uploads API

## Scope

- 교수자 또는 관리자의 파일 업로드 URL 발급
- 프론트는 presigned URL로 직접 S3에 업로드
- 업로드 완료 후 반환된 `fileUrl`을 강의 썸네일, 콘텐츠 URL, 프로필 이미지 등에 저장

## `POST /api/v1/uploads/presigned-url`

범용 S3 presigned `PUT` URL을 발급한다.

- 권한: `INSTRUCTOR`, `ADMIN`
- 응답의 `uploadUrl`은 짧은 시간만 유효하다.

Request:

```json
{
  "fileName": "week1-intro.mp4",
  "contentType": "video/mp4",
  "category": "CONTENT_VIDEO"
}
```

지원 `category` 예시:

- `COURSE_THUMBNAIL`
- `CONTENT_VIDEO`
- `CONTENT_DOCUMENT`
- `PROFILE_IMAGE`

Response:

```json
{
  "success": true,
  "message": "파일 업로드 URL 생성 완료",
  "data": {
    "uploadUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/...",
    "fileUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/content-videos/20260412/42/...",
    "objectKey": "content-videos/20260412/42/uuid-week1-intro.mp4",
    "expiresInSeconds": 900
  }
}
```

## `POST /api/v1/uploads/videos/presigned-url`

하위 호환용 비디오 전용 엔드포인트. 내부적으로 `category = CONTENT_VIDEO`로 처리한다.

## Frontend Flow

1. 사용자가 파일을 선택한다.
2. 프론트가 `POST /api/v1/uploads/presigned-url` 호출
3. 프론트가 응답 `uploadUrl`로 `PUT` 업로드 수행
4. 업로드 성공 후 응답 `fileUrl`을 적절한 필드에 저장한다.

예시:

- 강의 썸네일 생성 시 `thumbnailUrl`
- VOD 콘텐츠 생성 시 `videoUrl`
- 문서 콘텐츠 생성 시 `documentUrl`
- 마이페이지 수정 시 `profileImageUrl`

## Error Notes

- S3가 비활성화되었거나 설정이 누락되면 공통 실패 응답을 반환한다.
- 권한이 없으면 `403 COURSE_ACCESS_DENIED`
- 지원하지 않는 파일 형식이면 `400 INVALID_INPUT`
