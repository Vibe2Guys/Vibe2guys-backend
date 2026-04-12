# Uploads API

## Scope

- 교수자 또는 관리자의 비디오 업로드 URL 발급
- 프론트는 presigned URL로 직접 S3에 업로드
- 업로드 완료 후 반환된 `fileUrl`을 콘텐츠 `videoUrl`로 저장

## `POST /api/v1/uploads/videos/presigned-url`

S3 presigned PUT URL을 발급한다.

- 권한: `INSTRUCTOR`, `ADMIN`
- 지원 MIME 타입: `video/mp4`, `video/quicktime`, `video/webm`, `video/x-matroska`
- 응답의 `uploadUrl`은 짧은 시간만 유효하다.

Request:

```json
{
  "fileName": "week1-intro.mp4",
  "contentType": "video/mp4"
}
```

Response:

```json
{
  "success": true,
  "message": "비디오 업로드 URL 생성 완료",
  "data": {
    "uploadUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/...",
    "fileUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/videos/20260412/42/...",
    "objectKey": "videos/20260412/42/uuid-week1-intro.mp4",
    "expiresInSeconds": 900
  }
}
```

## Frontend Flow

1. 교수자가 파일을 선택한다.
2. 프론트가 `POST /api/v1/uploads/videos/presigned-url` 호출
3. 프론트가 응답 `uploadUrl`로 `PUT` 업로드 수행
4. 업로드 성공 후 응답 `fileUrl`을 콘텐츠 생성 API의 `videoUrl`에 넣어 저장

## Error Notes

- S3가 비활성화되었거나 설정이 누락되면 공통 실패 응답을 반환한다.
- 권한이 없으면 `403 COURSE_ACCESS_DENIED`
- 지원하지 않는 파일 형식이면 `400 INVALID_INPUT`
