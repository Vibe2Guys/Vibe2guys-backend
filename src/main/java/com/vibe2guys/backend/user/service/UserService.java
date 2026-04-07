package com.vibe2guys.backend.user.service;

import com.vibe2guys.backend.common.exception.BusinessException;
import com.vibe2guys.backend.common.exception.ErrorCode;
import com.vibe2guys.backend.user.domain.User;
import com.vibe2guys.backend.user.dto.MyProfileResponse;
import com.vibe2guys.backend.user.dto.UpdateMyProfileRequest;
import com.vibe2guys.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    public MyProfileResponse getMyProfile(Long userId) {
        return MyProfileResponse.from(getById(userId));
    }

    @Transactional
    public MyProfileResponse updateMyProfile(Long userId, UpdateMyProfileRequest request) {
        User user = getById(userId);
        user.updateProfile(request.name(), request.profileImageUrl());
        return MyProfileResponse.from(user);
    }
}
