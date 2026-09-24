package com.example.sqlix.service.authentication;

import com.example.sqlix.entity.RefreshToken;
import com.example.sqlix.entity.User;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.RefreshTokenRepository;
import com.example.sqlix.utils.TokenHashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    @Transactional
    public String createRefreshToken(User user, String userAgent, String ipAddress) {
        String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();

        String tokenHash = TokenHashUtils.hash(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .expiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS))
                .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String rawToken) {
        String tokenHash = TokenHashUtils.hash(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (refreshToken.getRevokedAt() != null) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        if (!Boolean.TRUE.equals(refreshToken.getUser().getIsActive())) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }
        return refreshToken;
    }

    @Transactional
    public void revokeToken(String rawToken, UUID currentUSerId) {
        String tokenHash = TokenHashUtils.hash(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!refreshToken.getUser().getId().equals(currentUSerId)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (refreshToken.getRevokedAt() == null) {
            refreshToken.setRevokedAt(Instant.now());

            refreshTokenRepository.save(refreshToken);
        }
    }

    @Transactional
    public String rotateRefreshToken(
            RefreshToken oldRefreshToken,
            String userAgent,
            String ipAddress
    ) {
        oldRefreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(oldRefreshToken);

        return createRefreshToken(
                oldRefreshToken.getUser(),
                userAgent,
                ipAddress
        );
    }

    @Transactional
    public void revokeAllTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
    }
}
