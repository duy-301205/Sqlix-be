package com.example.sqlix.service.authentication;

import com.example.sqlix.entity.PasswordResetToken;
import com.example.sqlix.entity.User;
import com.example.sqlix.event.OtpEmailEvent;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.PasswordResetTokenRepository;
import com.example.sqlix.repository.RefreshTokenRepository;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.utils.TokenHashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TOKEN_TTL_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT)).ifPresent(user -> {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            PasswordResetToken token = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(TokenHashUtils.hash(rawToken))
                    .expiresAt(Instant.now().plus(TOKEN_TTL_MINUTES, ChronoUnit.MINUTES))
                    .build();
            try {
                tokenRepository.saveAndFlush(token);
            } catch (DataIntegrityViolationException ex) {
                throw new AppException(ErrorCode.DATA_CONSTRAINT_VIOLATION);
            }
            eventPublisher.publishEvent(new OtpEmailEvent(user.getEmail(), rawToken, "PASSWORD_RESET"));
        });
        // Same result for known and unknown email to avoid account enumeration.
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHashForUpdate(TokenHashUtils.hash(rawToken))
                .orElseThrow(() -> new AppException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID));
        Instant now = Instant.now();
        if (token.getUsedAt() != null) {
            throw new AppException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
        }
        if (!token.getExpiresAt().isAfter(now)) {
            throw new AppException(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        token.setUsedAt(now);
        refreshTokenRepository.revokeAllByUserId(user.getId(), now);
    }
}
