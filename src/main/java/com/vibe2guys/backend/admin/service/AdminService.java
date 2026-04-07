package com.vibe2guys.backend.admin.service;

import com.vibe2guys.backend.admin.domain.AnalyticsConfig;
import com.vibe2guys.backend.admin.dto.AdminUserItemResponse;
import com.vibe2guys.backend.admin.dto.AnalyticsConfigResponse;
import com.vibe2guys.backend.admin.dto.CreateAdminUserRequest;
import com.vibe2guys.backend.admin.dto.CreateAdminUserResponse;
import com.vibe2guys.backend.admin.dto.UpdateAnalyticsConfigRequest;
import com.vibe2guys.backend.admin.repository.AnalyticsConfigRepository;
import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.common.response.PageResponse;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.domain.UserStatus;
import com.vibe2guys.backend.user.repository.UserRepository;
import com.vibe2guys.backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private static final long DEFAULT_ANALYTICS_CONFIG_ID = 1L;

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AnalyticsConfigRepository analyticsConfigRepository;

    public AdminService(
            UserService userService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AnalyticsConfigRepository analyticsConfigRepository
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.analyticsConfigRepository = analyticsConfigRepository;
    }

    public PageResponse<AdminUserItemResponse> getUsers(Long requesterId, int page, int size, String role, String keyword) {
        ensureAdmin(requesterId);
        Specification<User> specification = Specification.where((Specification<User>) null);
        if (role != null && !role.isBlank()) {
            UserRole parsedRole;
            try {
                parsedRole = UserRole.valueOf(role.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 사용자 역할입니다.");
            }
            specification = specification.and((root, query, cb) -> cb.equal(root.get("role"), parsedRole));
        }
        if (keyword != null && !keyword.isBlank()) {
            String normalizedKeyword = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), normalizedKeyword),
                            cb.like(cb.lower(root.get("email")), normalizedKeyword)
                    )
            );
        }
        Page<User> users = userRepository.findAll(specification, PageRequest.of(Math.max(page, 0), normalizeSize(size)));
        return PageResponse.from(users.map(AdminUserItemResponse::from));
    }

    @Transactional
    public CreateAdminUserResponse createUser(Long requesterId, CreateAdminUserRequest request) {
        ensureAdmin(requesterId);
        if (request.role() == UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "관리자 계정 생성은 별도 절차로 관리해야 합니다.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "이미 사용 중인 이메일입니다.");
        }
        User user = userRepository.save(User.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .build());
        return new CreateAdminUserResponse(user.getId());
    }

    public AnalyticsConfigResponse getAnalyticsConfig(Long requesterId) {
        ensureAdmin(requesterId);
        return AnalyticsConfigResponse.from(getAnalyticsConfigEntity());
    }

    @Transactional
    public AnalyticsConfigResponse updateAnalyticsConfig(Long requesterId, UpdateAnalyticsConfigRequest request) {
        ensureAdmin(requesterId);
        validateWeights(request);
        if (request.riskThresholdHigh() <= request.riskThresholdMedium()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "high threshold는 medium threshold보다 커야 합니다.");
        }
        AnalyticsConfig config = getAnalyticsConfigEntity();
        config.update(
                request.attendanceWeight(),
                request.progressWeight(),
                request.assignmentWeight(),
                request.quizWeight(),
                request.teamActivityWeight(),
                request.riskThresholdHigh(),
                request.riskThresholdMedium()
        );
        return AnalyticsConfigResponse.from(config);
    }

    public AnalyticsConfig getAnalyticsConfigEntity() {
        return analyticsConfigRepository.findById(DEFAULT_ANALYTICS_CONFIG_ID)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "analytics config가 초기화되지 않았습니다."));
    }

    private void ensureAdmin(Long requesterId) {
        User user = userService.getById(requesterId);
        if (user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "관리자만 접근할 수 있습니다.");
        }
    }

    private void validateWeights(UpdateAnalyticsConfigRequest request) {
        double total = request.attendanceWeight()
                + request.progressWeight()
                + request.assignmentWeight()
                + request.quizWeight()
                + request.teamActivityWeight();
        if (Math.abs(total - 1.0) > 0.0001) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "가중치 합계는 1.0이어야 합니다.");
        }
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, 100);
    }
}
