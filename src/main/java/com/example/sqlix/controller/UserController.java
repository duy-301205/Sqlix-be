package com.example.sqlix.controller;

import com.example.sqlix.dto.request.UpdateUserProfileRequest;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.CurrentUserResponse;
import com.example.sqlix.entity.User;
import com.example.sqlix.service.user.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {
    private final UserProfileService userProfileService;

    @GetMapping
    public ApiResponse<CurrentUserResponse> getCurrentUser(Authentication authentication) {
        return ApiResponse.<CurrentUserResponse>builder()
                .code(200)
                .result(userProfileService.getCurrentUser(currentUserEmail(authentication)))
                .build();
    }

    @PutMapping
    public ApiResponse<CurrentUserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return ApiResponse.<CurrentUserResponse>builder()
                .code(200)
                .message("Cập nhật hồ sơ thành công.")
                .result(userProfileService.updateCurrentUser(currentUserEmail(authentication), request))
                .build();
    }

    private String currentUserEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        return principal instanceof User user ? user.getEmail() : authentication.getName();
    }
}
