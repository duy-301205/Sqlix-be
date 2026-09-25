package com.example.sqlix.controller;

import com.example.sqlix.dto.request.LoginRequest;
import com.example.sqlix.dto.request.LogoutRequest;
import com.example.sqlix.dto.request.RefreshTokenRequest;
import com.example.sqlix.dto.request.RegisterRequest;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.LoginResponse;
import com.example.sqlix.dto.response.RefreshTokenResponse;
import com.example.sqlix.dto.response.RegisterResponse;
import com.example.sqlix.service.authentication.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

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
}
