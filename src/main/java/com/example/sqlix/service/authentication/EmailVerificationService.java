package com.example.sqlix.service.authentication;

import com.example.sqlix.entity.EmailVerificationToken;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.VerifyResult;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.EmailVerificationTokenRepository;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.service.email.EmailService;
import com.example.sqlix.utils.OtpHashUtils;
import com.example.sqlix.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OtpHashUtils otpHashUtils;

    private static final String PURPOSE = "EMAIL_VERIFICATION";
    private static final int MAX_ATTEMPTS = 5;
    private static final int OTP_EXPIRATION_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    @Transactional
    public VerifyResult verifyEmail(String email, String otp) {

        User user = userRepository.findByEmailForUpdate(email)
                .orElse(null);

        if (user == null) {
            return VerifyResult.USER_NOT_FOUND;
        }

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return VerifyResult.ALREADY_VERIFIED;
        }

        List<EmailVerificationToken> tokens =
                tokenRepository.findUnusedTokens(user.getId());

        if (tokens.isEmpty()) {
            return VerifyResult.INVALID_OTP;
        }

        EmailVerificationToken token = tokens.get(0);

        Instant now = Instant.now();

        if (!token.getExpiresAt().isAfter(now)) {
            return VerifyResult.EXPIRED_OTP;
        }

        if (token.getAttemptCount() >= MAX_ATTEMPTS) {
            return VerifyResult.TOO_MANY_ATTEMPTS;
        }

        boolean valid = otpHashUtils.matches(
                user.getId(),
                PURPOSE,
                otp,
                token.getTokenHash()
        );

        if (!valid) {
            token.setAttemptCount(token.getAttemptCount() + 1);

            return token.getAttemptCount() >= MAX_ATTEMPTS
                    ? VerifyResult.TOO_MANY_ATTEMPTS
                    : VerifyResult.INVALID_OTP;
        }

        user.setEmailVerified(true);
        user.setUpdatedAt(now);
        token.setUsedAt(now);

        return VerifyResult.SUCCESS;
    }

    private String createOtp(User user, Instant now) {
        String otp = OtpUtils.generateOtp();

        EmailVerificationToken token =
                EmailVerificationToken.builder()
                        .user(user)
                        .tokenHash(
                                otpHashUtils.hash(
                                        user.getId(),
                                        PURPOSE,
                                        otp
                                )
                        )
                        .expiresAt(
                                now.plus(
                                        OTP_EXPIRATION_MINUTES,
                                        ChronoUnit.MINUTES
                                )
                        )
                        .attemptCount(0)
                        .createdAt(now)
                        .build();

        tokenRepository.save(token);

        return otp;
    }

    // Gui OTP lan dau
    @Transactional
    public String sendInitialVerification(User user) {

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException(
                    "Email already verified"
            );
        }

        Instant now = Instant.now();

        String otp = createOtp(user, now);

        return otp;
    }

    @Transactional
    public String resendVerification(UUID userId) {

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalStateException(
                    "Email already verified"
            );
        }

        Instant now = Instant.now();

        tokenRepository
                .findTopByUser_IdOrderByCreatedAtDesc(userId)
                .ifPresent(lastToken -> {
                    if (lastToken.getCreatedAt()
                            .plusSeconds(RESEND_COOLDOWN_SECONDS)
                            .isAfter(now)) {
                        throw new IllegalStateException(
                                "Please wait 60 seconds"
                        );
                    }
                });

        List<EmailVerificationToken> oldTokens =
                tokenRepository.findUnusedTokens(userId);

        for (EmailVerificationToken token : oldTokens) {
            token.setUsedAt(now);
        }

        String otp = createOtp(user, now);

        return otp;
    }
}
