package com.example.sqlix.service.authentication;

import com.example.sqlix.entity.EmailVerificationToken;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.VerifyResult;
import com.example.sqlix.repository.EmailVerificationTokenRepository;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.service.email.EmailService;
import com.example.sqlix.utils.OtpHashUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {
    @Mock EmailVerificationTokenRepository tokens;
    @Mock UserRepository users;
    @Mock EmailService email;
    @Mock OtpHashUtils hashes;
    @Mock ApplicationEventPublisher events;
    @InjectMocks EmailVerificationService service;

    @Test void verifiesValidOtpAndMarksUserVerified() {
        UUID id = UUID.randomUUID(); User user = User.builder().id(id).emailVerified(false).build();
        EmailVerificationToken token = EmailVerificationToken.builder().id(UUID.randomUUID()).user(user)
                .expiresAt(Instant.now().plusSeconds(90)).attemptCount(0).tokenHash("hash").build();
        when(users.findByEmailForUpdate("u@example.com")).thenReturn(Optional.of(user));
        when(tokens.findUnusedTokens(id)).thenReturn(List.of(token));
        when(hashes.matches(id, token.getId(), "EMAIL_VERIFICATION", "123456", "hash")).thenReturn(true);
        assertEquals(VerifyResult.SUCCESS, service.verifyEmail("u@example.com", "123456"));
        assertTrue(user.getEmailVerified());
        assertNotNull(token.getUsedAt());
    }

    @Test void wrongOtpIncrementsAttemptCount() {
        UUID id = UUID.randomUUID(); User user = User.builder().id(id).emailVerified(false).build();
        EmailVerificationToken token = EmailVerificationToken.builder().id(UUID.randomUUID()).user(user)
                .expiresAt(Instant.now().plusSeconds(90)).attemptCount(0).tokenHash("hash").build();
        when(users.findByEmailForUpdate("u@example.com")).thenReturn(Optional.of(user));
        when(tokens.findUnusedTokens(id)).thenReturn(List.of(token));
        when(hashes.matches(any(), any(), anyString(), anyString(), anyString())).thenReturn(false);
        assertEquals(VerifyResult.INVALID_OTP, service.verifyEmail("u@example.com", "000000"));
        assertEquals(1, token.getAttemptCount());
    }

    @Test void expiredOtpIsRejected() {
        UUID id = UUID.randomUUID(); User user = User.builder().id(id).emailVerified(false).build();
        EmailVerificationToken token = EmailVerificationToken.builder().id(UUID.randomUUID()).user(user)
                .expiresAt(Instant.now().minusSeconds(1)).attemptCount(0).tokenHash("hash").build();
        when(users.findByEmailForUpdate("u@example.com")).thenReturn(Optional.of(user));
        when(tokens.findUnusedTokens(id)).thenReturn(List.of(token));
        assertEquals(VerifyResult.EXPIRED_OTP, service.verifyEmail("u@example.com", "123456"));
        verifyNoInteractions(hashes);
    }
}
