package com.vibe2guys.backend.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.common.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Component
public class BackofficeAccessFilter extends OncePerRequestFilter {

    private final BackofficeAccessProperties backofficeAccessProperties;
    private final ObjectMapper objectMapper;

    public BackofficeAccessFilter(BackofficeAccessProperties backofficeAccessProperties, ObjectMapper objectMapper) {
        this.backofficeAccessProperties = backofficeAccessProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String pathPrefix = backofficeAccessProperties.pathPrefix();
        if (pathPrefix == null || pathPrefix.isBlank() || !request.getRequestURI().startsWith(pathPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrincipal;
        if (!authenticated || authentication.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            writeErrorResponse(response, ErrorCode.COURSE_ACCESS_DENIED, "백오피스는 관리자만 접근할 수 있습니다.");
            return;
        }

        String expectedAccessKey = backofficeAccessProperties.accessKey();
        String receivedAccessKey = request.getHeader(backofficeAccessProperties.headerName());
        if (!matchesAccessKey(expectedAccessKey, receivedAccessKey)) {
            writeErrorResponse(response, ErrorCode.AUTH_REQUIRED, "백오피스 접근 키가 올바르지 않습니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean matchesAccessKey(String expectedAccessKey, String receivedAccessKey) {
        if (expectedAccessKey == null || expectedAccessKey.isBlank() || receivedAccessKey == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expectedAccessKey.getBytes(StandardCharsets.UTF_8),
                receivedAccessKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(errorCode, message));
    }
}
