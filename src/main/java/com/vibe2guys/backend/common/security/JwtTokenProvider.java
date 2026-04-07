package com.vibe2guys.backend.common.security;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.accessTokenExpirationSeconds());
        return Jwts.builder()
                .subject(String.valueOf(principal.getId()))
                .claim("type", "access")
                .claim("email", principal.getEmail())
                .claim("role", principal.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.refreshTokenExpirationSeconds());
        return Jwts.builder()
                .subject(String.valueOf(principal.getId()))
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public void validateAccessToken(String token) {
        Claims claims = parseAndValidate(token);
        String type = claims.get("type", String.class);
        if (!Objects.equals(type, "access")) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "access token이 아닙니다.");
        }
    }

    public void validateRefreshToken(String token) {
        Claims claims = parseAndValidate(token);
        String type = claims.get("type", String.class);
        if (!Objects.equals(type, "refresh")) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "refresh token이 아닙니다.");
        }
    }

    private Claims parseAndValidate(String token) {
        try {
            return parseClaims(token);
        } catch (ExpiredJwtException ex) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED, "토큰이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "유효하지 않은 토큰입니다.");
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
