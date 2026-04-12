package com.vibe2guys.backend.storage.service;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.storage.config.S3StorageProperties;
import com.vibe2guys.backend.storage.dto.CreateFileUploadUrlRequest;
import com.vibe2guys.backend.storage.dto.FileUploadCategory;
import com.vibe2guys.backend.storage.dto.FileUploadUrlResponse;
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
public class FileUploadService {

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of(
            "video/mp4",
            "video/quicktime",
            "video/webm",
            "video/x-matroska"
    );

    private static final Set<String> DOCUMENT_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    private final S3StorageProperties properties;
    private final ObjectProvider<S3Presigner> s3PresignerProvider;
    private final UserService userService;

    public FileUploadUrlResponse createUploadUrl(Long requesterId, CreateFileUploadUrlRequest request) {
        User user = userService.getById(requesterId);
        validateUploadAccess(user, request.category());
        validateStorageEnabled();

        String contentType = request.contentType().trim().toLowerCase();
        validateContentType(request.category(), contentType);

        S3Presigner s3Presigner = s3PresignerProvider.getIfAvailable();
        if (s3Presigner == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 업로드 초기화에 실패했습니다.");
        }

        String objectKey = buildObjectKey(user.getId(), request.category(), request.fileName());
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

        return new FileUploadUrlResponse(
                presignedRequest.url().toString(),
                resolvePublicFileUrl(objectKey),
                objectKey,
                properties.presignExpirationSeconds()
        );
    }

    private void validateUploadAccess(User user, FileUploadCategory category) {
        if (category == FileUploadCategory.PROFILE_IMAGE) {
            return;
        }
        if (user.getRole() != UserRole.INSTRUCTOR && user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "교수자 또는 관리자만 강의 파일을 업로드할 수 있습니다.");
        }
    }

    private void validateStorageEnabled() {
        if (!properties.enabled()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 업로드가 활성화되지 않았습니다.");
        }
        if (properties.bucket() == null || properties.bucket().isBlank()
                || properties.region() == null || properties.region().isBlank()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 설정이 올바르지 않습니다.");
        }
    }

    private void validateContentType(FileUploadCategory category, String contentType) {
        Set<String> allowedTypes = switch (category) {
            case COURSE_THUMBNAIL, PROFILE_IMAGE -> IMAGE_CONTENT_TYPES;
            case CONTENT_VIDEO -> VIDEO_CONTENT_TYPES;
            case CONTENT_DOCUMENT -> DOCUMENT_CONTENT_TYPES;
        };
        if (!allowedTypes.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, buildInvalidTypeMessage(category));
        }
    }

    private String buildInvalidTypeMessage(FileUploadCategory category) {
        return switch (category) {
            case COURSE_THUMBNAIL, PROFILE_IMAGE -> "지원하지 않는 이미지 형식입니다.";
            case CONTENT_VIDEO -> "지원하지 않는 비디오 형식입니다.";
            case CONTENT_DOCUMENT -> "지원하지 않는 문서 형식입니다.";
        };
    }

    private String buildObjectKey(Long userId, FileUploadCategory category, String originalFileName) {
        String safeFileName = sanitizeFileName(originalFileName);
        return buildCategoryPrefix(category) + "/"
                + DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now()) + "/"
                + userId + "/"
                + UUID.randomUUID() + "-" + safeFileName;
    }

    private String buildCategoryPrefix(FileUploadCategory category) {
        String uploadPrefix = properties.uploadPrefix() == null || properties.uploadPrefix().isBlank()
                ? "uploads"
                : properties.uploadPrefix().replaceAll("^/+", "").replaceAll("/+$", "");
        return switch (category) {
            case COURSE_THUMBNAIL -> uploadPrefix + "/course-thumbnails";
            case CONTENT_VIDEO -> uploadPrefix + "/videos";
            case CONTENT_DOCUMENT -> uploadPrefix + "/documents";
            case PROFILE_IMAGE -> uploadPrefix + "/profiles";
        };
    }

    private String sanitizeFileName(String originalFileName) {
        String trimmed = originalFileName == null ? "file" : originalFileName.trim();
        String fallback = trimmed.isBlank() ? "file" : trimmed;
        return URLEncoder.encode(fallback, StandardCharsets.UTF_8).replace("+", "_");
    }

    private String resolvePublicFileUrl(String objectKey) {
        if (properties.publicBaseUrl() != null && !properties.publicBaseUrl().isBlank()) {
            return properties.publicBaseUrl().replaceAll("/+$", "") + "/" + objectKey;
        }
        return "https://" + properties.bucket() + ".s3." + properties.region() + ".amazonaws.com/" + objectKey;
    }
}
