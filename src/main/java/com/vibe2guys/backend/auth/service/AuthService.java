package com.vibe2guys.backend.auth.service;

import com.vibe2guys.backend.auth.domain.RefreshToken;
import com.vibe2guys.backend.auth.dto.AuthUserResponse;
import com.vibe2guys.backend.auth.dto.LoginRequest;
import com.vibe2guys.backend.auth.dto.LoginResponse;
import com.vibe2guys.backend.auth.dto.LogoutRequest;
import com.vibe2guys.backend.auth.dto.LogoutResponse;
import com.vibe2guys.backend.auth.dto.RegisterRequest;
import com.vibe2guys.backend.auth.dto.RegisterResponse;
import com.vibe2guys.backend.auth.dto.TokenRefreshRequest;
import com.vibe2guys.backend.auth.dto.TokenRefreshResponse;
import com.vibe2guys.backend.auth.repository.RefreshTokenRepository;
import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.common.security.CustomUserDetailsService;
import com.vibe2guys.backend.common.security.JwtProperties;
import com.vibe2guys.backend.common.security.JwtTokenProvider;
import com.vibe2guys.backend.common.security.RefreshTokenHasher;
import com.vibe2guys.backend.common.security.SecurityNetworkProperties;
import com.vibe2guys.backend.common.security.UserPrincipal;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.domain.UserRole;
import com.vibe2guys.backend.user.domain.UserStatus;
import com.vibe2guys.backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenHasher refreshTokenHasher;
    private final LoginThrottleService loginThrottleService;
    private final SecurityNetworkProperties securityNetworkProperties;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String normalizedName = request.name().trim();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "이미 사용 중인 이메일입니다.");
        }
        if (request.role() != UserRole.STUDENT) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "MVP에서는 학생만 직접 회원가입할 수 있습니다.");
        }
        User user = userRepository.save(User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .name(normalizedName)
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .build());
        return RegisterResponse.from(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        String normalizedEmail = normalizeEmail(request.email());
        String clientIp = resolveClientIp(httpServletRequest);
        OffsetDateTime blockedUntil = loginThrottleService.checkAllowed(normalizedEmail, clientIp);
        if (blockedUntil != null) {
            throw buildRateLimitedException(blockedUntil);
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
            );
        } catch (AuthenticationException ex) {
            OffsetDateTime newBlockedUntil = loginThrottleService.recordFailure(normalizedEmail, clientIp);
            if (newBlockedUntil != null && newBlockedUntil.isAfter(OffsetDateTime.now())) {
                throw buildRateLimitedException(newBlockedUntil);
            }
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        loginThrottleService.recordSuccess(normalizedEmail, clientIp);

        UserPrincipal principal = (UserPrincipal) customUserDetailsService.loadUserByUsername(normalizedEmail);
        String accessToken = jwtTokenProvider.createAccessToken(principal);
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(principal);
        String refreshTokenHash = refreshTokenHasher.hash(refreshTokenValue);
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(refreshTokenHash)
                .deviceName(httpServletRequest.getHeader("User-Agent"))
                .ipAddress(clientIp)
                .expiresAt(OffsetDateTime.now().plusSeconds(jwtProperties.refreshTokenExpirationSeconds()))
                .build());

        return new LoginResponse(accessToken, refreshTokenValue, AuthUserResponse.from(user));
    }

    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        jwtTokenProvider.validateRefreshToken(request.refreshToken());
        String refreshTokenHash = refreshTokenHasher.hash(request.refreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID, "refresh token을 찾을 수 없습니다."));

        OffsetDateTime now = OffsetDateTime.now();
        if (refreshToken.isRevoked() || refreshToken.isExpired(now)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "사용할 수 없는 refresh token입니다.");
        }

        UserPrincipal principal = customUserDetailsService.loadUserById(refreshToken.getUser().getId());
        String newAccessToken = jwtTokenProvider.createAccessToken(principal);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(principal);
        String newRefreshTokenHash = refreshTokenHasher.hash(newRefreshToken);

        refreshToken.revoke(now);
        refreshTokenRepository.save(RefreshToken.builder()
                .user(refreshToken.getUser())
                .tokenHash(newRefreshTokenHash)
                .deviceName(refreshToken.getDeviceName())
                .ipAddress(refreshToken.getIpAddress())
                .expiresAt(now.plusSeconds(jwtProperties.refreshTokenExpirationSeconds()))
                .build());

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public LogoutResponse logout(Long userId, LogoutRequest request) {
        jwtTokenProvider.validateRefreshToken(request.refreshToken());
        String refreshTokenHash = refreshTokenHasher.hash(request.refreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndUserId(refreshTokenHash, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID, "사용자 세션과 일치하는 refresh token이 없습니다."));

        OffsetDateTime now = OffsetDateTime.now();
        if (!refreshToken.isRevoked()) {
            refreshToken.revoke(now);
        }
        return new LogoutResponse(true, refreshToken.getRevokedAt());
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (securityNetworkProperties.trustProxyHeaders()) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",")[0].trim();
            }
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return realIp.trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private BusinessException buildRateLimitedException(OffsetDateTime blockedUntil) {
        long retryAfterSeconds = Math.max(1, ChronoUnit.SECONDS.between(OffsetDateTime.now(), blockedUntil));
        return new BusinessException(
                ErrorCode.LOGIN_RATE_LIMITED,
                "로그인 시도가 너무 많습니다. " + retryAfterSeconds + "초 후 다시 시도하세요."
        );
    }
}
