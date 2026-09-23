package com.example.sqlix.controller;

import com.example.sqlix.dto.request.LoginRequest;
import com.example.sqlix.dto.request.RegisterRequest;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.LoginResponse;
import com.example.sqlix.dto.response.RegisterResponse;
import com.example.sqlix.service.authentication.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
            @Valid @RequestBody LoginRequest request
    ) {

        return ApiResponse.<LoginResponse>builder()
                .message("Login successfully")
                .code(200)
                .result(authenticationService.login(request))
                .build();
    }
}
