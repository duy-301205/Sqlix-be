package com.example.sqlix.service.user;

import com.example.sqlix.dto.request.UpdateUserProfileRequest;
import com.example.sqlix.dto.response.CurrentUserResponse;
import com.example.sqlix.entity.User;
import com.example.sqlix.entity.UserProfile;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(String email) {
        User user = findUserWithProfile(email);
        return toResponse(user, user.getProfile());
    }

    @Transactional
    public CurrentUserResponse updateCurrentUser(String email, UpdateUserProfileRequest request) {
        User user = findUserWithProfile(email);
        UserProfile profile = user.getProfile();
        if (profile == null) {
            throw new AppException(ErrorCode.PROFILE_NOT_EXISTED);
        }

        user.setFullName(request.getFullName().trim());
        user.setAvatarUrl(normalizeOptional(request.getAvatarUrl()));
        user.setOccupation(request.getOccupation());
        profile.setBio(normalizeOptional(request.getBio()));
        profile.setDailyTargetMinutes(request.getDailyTargetMinutes());

        try {
            userRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.DATA_CONSTRAINT_VIOLATION);
        }
        return toResponse(user, profile);
    }

    private User findUserWithProfile(String email) {
        return userRepository.findByEmailWithProfile(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CurrentUserResponse toResponse(User user, UserProfile profile) {
        return CurrentUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getSystemRole())
                .occupation(user.getOccupation())
                .currentStreak(profile == null ? null : profile.getCurrentStreak())
                .longestStreak(profile == null ? null : profile.getLongestStreak())
                .dailyTargetMinutes(profile == null ? null : profile.getDailyTargetMinutes())
                .bio(profile == null ? null : profile.getBio())
                .build();
    }
}
