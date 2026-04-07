package com.vibe2guys.backend.auth.controller;

import com.vibe2guys.backend.auth.dto.LoginRequest;
import com.vibe2guys.backend.auth.dto.LoginResponse;
import com.vibe2guys.backend.auth.dto.RegisterRequest;
import com.vibe2guys.backend.auth.dto.RegisterResponse;
import com.vibe2guys.backend.auth.dto.TokenRefreshRequest;
import com.vibe2guys.backend.auth.dto.TokenRefreshResponse;
import com.vibe2guys.backend.auth.service.AuthService;
import com.vibe2guys.backend.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("회원가입 완료", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ApiResponse.success("로그인 성공", authService.login(request, httpServletRequest));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ApiResponse.success("토큰 재발급 성공", authService.refresh(request));
    }
}
