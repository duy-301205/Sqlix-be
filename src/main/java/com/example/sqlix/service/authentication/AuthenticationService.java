package com.example.sqlix.service.authentication;

import com.example.sqlix.dto.request.LoginRequest;
import com.example.sqlix.dto.request.LogoutRequest;
import com.example.sqlix.dto.request.RefreshTokenRequest;
import com.example.sqlix.dto.request.RegisterRequest;
import com.example.sqlix.dto.response.LoginResponse;
import com.example.sqlix.dto.response.RefreshTokenResponse;
import com.example.sqlix.dto.response.RegisterResponse;
import com.example.sqlix.entity.RefreshToken;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.UserSystemRole;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.RefreshTokenRepository;
import com.example.sqlix.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // 1. Kiểm tra username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // 2. Kiểm tra email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // 3. Kiểm tra điều khoản
        if (!Boolean.TRUE.equals(request.getTermsAccepted())) {
            throw new AppException(ErrorCode.TERMS_NOT_ACCEPTED);
        }

        // 4. Tạo user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(
                        passwordEncoder.encode(request.getPassword())
                )
                .fullName(request.getFullName())
                .occupation(request.getOccupation())

                // Không cho client tự chọn
                .systemRole(UserSystemRole.USER)
                .termsAccepted(true)
                .isActive(true)
                .build();

        // 5. Lưu database
        User savedUser = userRepository.saveAndFlush(user);

        // 6. Response
        return RegisterResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .occupation(savedUser.getOccupation())
                .systemRole(savedUser.getSystemRole())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    public LoginResponse login(LoginRequest request,
                               String userAgent,
                               String ipAddress) {

        // Có thể login bằng username hoặc email
        User user = userRepository
                .findByUsernameOrEmail(
                        request.getIdentifier(),
                        request.getIdentifier()
                )
                .orElseThrow(
                        () -> new AppException(ErrorCode.UNAUTHENTICATED)
                );

        // Account bị khóa
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        // Kiểm tra password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Sinh JWT
        String accessToken =
                jwtService.generateAccessToken(user);

        String refreshToken = refreshTokenService.createRefreshToken(
                user, userAgent,ipAddress);

        return LoginResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public RefreshTokenResponse refreshToken(
            RefreshTokenRequest request,
            String userAgent,
            String ipAddress
    ) {
        RefreshToken oldRefreshToken =
                refreshTokenService.verifyRefreshToken(
                        request.getRefreshToken()
                );

        User user = oldRefreshToken.getUser();

        String newAccessToken =
                jwtService.generateAccessToken(user);

        String newRefreshToken =
                refreshTokenService.rotateRefreshToken(
                        oldRefreshToken,
                        userAgent,
                        ipAddress
                );

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(LogoutRequest request,
                       String identifier) {
        User user = userRepository.findByUsernameOrEmail(
                identifier, identifier
        ).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        refreshTokenService.revokeToken(request.getRefreshToken(), user.getId());
    }

    @Transactional
    public void logoutAll(String identifier) {

        User user = userRepository
                .findByUsernameOrEmail(
                        identifier,
                        identifier
                )
                .orElseThrow(
                        () -> new AppException(
                                ErrorCode.UNAUTHENTICATED
                        )
                );

        refreshTokenService.revokeAllTokens(
                user.getId()
        );
    }
}
