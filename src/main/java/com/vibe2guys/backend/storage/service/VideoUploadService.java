package com.vibe2guys.backend.storage.service;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.storage.config.S3StorageProperties;
import com.vibe2guys.backend.storage.dto.CreateVideoUploadUrlRequest;
import com.vibe2guys.backend.storage.dto.VideoUploadUrlResponse;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoUploadService {

    private static final Set<String> ALLOWED_VIDEO_CONTENT_TYPES = Set.of(
            "video/mp4",
            "video/quicktime",
            "video/webm",
            "video/x-matroska"
    );

    private final S3StorageProperties properties;
    private final ObjectProvider<S3Presigner> s3PresignerProvider;
    private final UserService userService;

    public VideoUploadUrlResponse createVideoUploadUrl(Long requesterId, CreateVideoUploadUrlRequest request) {
        User user = userService.getById(requesterId);
        if (user.getRole() != UserRole.INSTRUCTOR && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "교수자 또는 관리자만 영상을 업로드할 수 있습니다.");
        }
        if (!properties.enabled()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 업로드가 활성화되지 않았습니다.");
        }
        if (properties.bucket() == null || properties.bucket().isBlank() || properties.region() == null || properties.region().isBlank()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 설정이 올바르지 않습니다.");
        }
        S3Presigner s3Presigner = s3PresignerProvider.getIfAvailable();
        if (s3Presigner == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 업로드 초기화에 실패했습니다.");
        }
        String contentType = request.contentType().trim().toLowerCase();
        if (!ALLOWED_VIDEO_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 비디오 형식입니다.");
        }

        String objectKey = buildObjectKey(user.getId(), request.fileName());
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.presignExpirationSeconds()))
                .putObjectRequest(putObjectRequest)
                .build();
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new VideoUploadUrlResponse(
                presignedRequest.url().toString(),
                resolvePublicFileUrl(objectKey),
                objectKey,
                properties.presignExpirationSeconds()
        );
    }

    private String buildObjectKey(Long userId, String originalFileName) {
        String uploadPrefix = properties.uploadPrefix() == null || properties.uploadPrefix().isBlank()
                ? "videos"
                : properties.uploadPrefix().replaceAll("^/+", "").replaceAll("/+$", "");
        String safeFileName = sanitizeFileName(originalFileName);
        return uploadPrefix + "/"
                + DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now()) + "/"
                + userId + "/"
                + UUID.randomUUID() + "-" + safeFileName;
    }

    private String sanitizeFileName(String originalFileName) {
        String trimmed = originalFileName == null ? "video.mp4" : originalFileName.trim();
        String fallback = trimmed.isBlank() ? "video.mp4" : trimmed;
        return URLEncoder.encode(fallback, StandardCharsets.UTF_8).replace("+", "_");
    }

    private String resolvePublicFileUrl(String objectKey) {
        if (properties.publicBaseUrl() != null && !properties.publicBaseUrl().isBlank()) {
            return properties.publicBaseUrl().replaceAll("/+$", "") + "/" + objectKey;
        }
        return "https://" + properties.bucket() + ".s3." + properties.region() + ".amazonaws.com/" + objectKey;
    }
}
