package com.example.sqlix.service.authentication;

import com.example.sqlix.entity.EmailVerificationToken;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.VerifyResult;
import com.example.sqlix.event.OtpEmailEvent;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.EmailVerificationTokenRepository;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.service.email.EmailService;
import com.example.sqlix.utils.OtpHashUtils;
import com.example.sqlix.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ApplicationEventPublisher eventPublisher;

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
                token.getId(),
                PURPOSE,
                otp,
                token.getTokenHash()
        );

        if (!valid) {
            token.setAttemptCount(
                    token.getAttemptCount() + 1
            );

            return token.getAttemptCount() >= MAX_ATTEMPTS
                    ? VerifyResult.TOO_MANY_ATTEMPTS
                    : VerifyResult.INVALID_OTP;
        }

        // Xác minh thành công
        user.setEmailVerified(true);
        user.setUpdatedAt(now);

        // Vô hiệu hóa toàn bộ OTP còn lại
        for (EmailVerificationToken oldToken : tokens) {
            oldToken.setUsedAt(now);
        }

        return VerifyResult.SUCCESS;
    }

    private void createOtp(User user, Instant now) {
        String otp = OtpUtils.generateOtp();

        UUID tokenId = UUID.randomUUID();

        EmailVerificationToken token =
                EmailVerificationToken.builder()
                        .id(tokenId)
                        .user(user)
                        .tokenHash(otpHashUtils.hash(user.getId(), tokenId, PURPOSE, otp))
                        .expiresAt(now.plus(OTP_EXPIRATION_MINUTES, ChronoUnit.MINUTES))
                        .attemptCount(0)
                        .createdAt(now)
                        .build();

        tokenRepository.save(token);

        eventPublisher.publishEvent(
                new OtpEmailEvent(
                        user.getEmail(),
                        otp,
                        PURPOSE
                )
        );
    }

    // Gui OTP lan dau sau dang ky
    @Transactional
    public void sendInitialVerification(User user) {

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AppException(
                    ErrorCode.EMAIL_ALREADY_VERIFIED
            );
        }

        createOtp(user, Instant.now());
    }

    @Transactional
    public void resendVerification(UUID userId) {

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_FOUND)
                );

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AppException(
                    ErrorCode.EMAIL_ALREADY_VERIFIED
            );
        }

        Instant now = Instant.now();

        tokenRepository.findTopByUser_IdOrderByCreatedAtDesc(userId)
                .ifPresent(lastToken -> {
                    if (lastToken.getCreatedAt()
                            .plusSeconds(RESEND_COOLDOWN_SECONDS)
                            .isAfter(now)) {

                        throw new AppException(
                                ErrorCode.OTP_RESEND_TOO_SOON
                        );
                    }
                });

        // Vô hiệu hóa toàn bộ OTP cũ
        List<EmailVerificationToken> oldTokens =
                tokenRepository.findUnusedTokens(userId);

        for (EmailVerificationToken token : oldTokens) {
            token.setUsedAt(now);
        }

        // Tạo OTP mới và gửi email sau commit
        createOtp(user, now);
    }
}
