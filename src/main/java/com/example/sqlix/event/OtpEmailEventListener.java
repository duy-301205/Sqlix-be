package com.example.sqlix.event;

import com.example.sqlix.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpEmailEventListener {
    private final EmailService emailService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleOtpEmail(OtpEmailEvent event) {

        try {
            switch (event.purpose()) {
                case "EMAIL_VERIFICATION" ->
                        emailService.sendVerificationOtp(
                                event.email(),
                                event.otp()
                        );

                case "PASSWORD_RESET" ->
                        emailService.sendPasswordResetOtp(
                                event.email(),
                                event.otp()
                        );

                default -> throw new IllegalArgumentException(
                        "Unsupported OTP purpose"
                );
            }

        } catch (Exception e) {
            log.error(
                    "Failed to send OTP email to {}",
                    event.email(),
                    e
            );
        }
    }
}
