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
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

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
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) LogoutRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String refreshToken = request == null ? null : request.getRefreshToken();
        String cookieToken = null;
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    cookieToken = cookie.getValue();
                    break;
                }
            }
        }
        boolean clearCookie = cookieToken != null;
        if (refreshToken == null || refreshToken.isBlank()) refreshToken = cookieToken;
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_IS_MISSING);
        }
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setRefreshToken(refreshToken);
        authenticationService.logout(logoutRequest, authentication.getName());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(200)
                .message("Logout successful")
                .build();
        if (clearCookie) {
            ResponseCookie expiredCookie = ResponseCookie.from("refresh_token", "")
                    .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(0).build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, expiredCookie.toString()).body(response);
        }
        return ResponseEntity.ok(response);
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
