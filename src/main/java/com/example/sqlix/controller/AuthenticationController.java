package com.example.sqlix.controller;

import com.example.sqlix.dto.request.*;
import com.example.sqlix.dto.response.LoginResponse;
import com.example.sqlix.dto.response.RefreshTokenResponse;
import com.example.sqlix.dto.response.RegisterResponse;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.service.authentication.PasswordService;
import com.example.sqlix.service.authentication.OAuthAuthenticationService;
import com.example.sqlix.enums.VerifyResult;
import com.example.sqlix.service.authentication.AuthenticationService;
import com.example.sqlix.service.authentication.EmailVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordService passwordService;
    private final OAuthAuthenticationService oauthAuthenticationService;

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordService.requestPasswordReset(request.getEmail());
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Nếu email tồn tại, hướng dẫn đặt lại mật khẩu sẽ được gửi.").build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request.getToken(), request.getNewPassword());
        return ApiResponse.<Void>builder().code(200).message("Đặt lại mật khẩu thành công.").build();
    }

    @PostMapping("/oauth")
    public ApiResponse<LoginResponse> oauthLogin(@Valid @RequestBody OAuthLoginRequest request,
                                                 HttpServletRequest httpRequest) {
        LoginResponse response = oauthAuthenticationService.login(request.getProvider(), request.getAccessToken(),
                httpRequest.getHeader("User-Agent"), httpRequest.getRemoteAddr());
        return ApiResponse.<LoginResponse>builder().code(200).message("Login successfully").result(response).build();
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        return ApiResponse.<RegisterResponse>builder()
                .message("Register successfully")
                .code(200)
                .result(authenticationService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        String userAgent = httpRequest.getHeader("User-Agent");

        String ipAddress = httpRequest.getRemoteAddr();

        return ApiResponse.<LoginResponse>builder()
                .message("Login successfully")
                .code(200)
                .result(authenticationService.login(request,userAgent, ipAddress))
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refreshToken(
            @RequestBody @Valid RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        String userAgent =
                httpRequest.getHeader("User-Agent");

        String ipAddress =
                httpRequest.getRemoteAddr();

        RefreshTokenResponse result =
                authenticationService.refreshToken(
                        request,
                        userAgent,
                        ipAddress
                );

        return ApiResponse.<RefreshTokenResponse>builder()
                .code(200)
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody @Valid LogoutRequest request,
                                    Authentication authentication) {
        authenticationService.logout(request, authentication.getName());

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Logout successful")
                .build();
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(
            Authentication authentication
    ) {

        authenticationService.logoutAll(
                authentication.getName()
        );

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Logout from all devices successful")
                .build();
    }

    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(
            @RequestBody @Valid VerifyEmailRequest request
    ) {
        authenticationService.verifyEmail(request);

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Email verified successfully")
                .build();
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Void> resendVerification(
            Authentication authentication
    ) {
        authenticationService.resendVerification(
                authentication.getName()
        );

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Verification OTP sent successfully")
                .build();
    }
}
