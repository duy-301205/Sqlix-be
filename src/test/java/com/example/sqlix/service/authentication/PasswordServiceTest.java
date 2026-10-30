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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {
    @Mock UserRepository users;
    @Mock PasswordResetTokenRepository tokens;
    @Mock RefreshTokenRepository refreshTokens;
    @Mock PasswordEncoder encoder;
    @Mock ApplicationEventPublisher events;
    @InjectMocks PasswordService service;

    @Test void forgotPasswordPersistsOnlyHashAndPublishesEmailEvent() {
        User user = User.builder().id(UUID.randomUUID()).email("person@example.com").build();
        when(users.findByEmail("person@example.com")).thenReturn(Optional.of(user));
        service.requestPasswordReset("person@example.com");
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokens).saveAndFlush(tokenCaptor.capture());
        ArgumentCaptor<OtpEmailEvent> eventCaptor = ArgumentCaptor.forClass(OtpEmailEvent.class);
        verify(events).publishEvent(eventCaptor.capture());
        assertNotEquals(eventCaptor.getValue().otp(), tokenCaptor.getValue().getTokenHash());
        assertEquals(TokenHashUtils.hash(eventCaptor.getValue().otp()), tokenCaptor.getValue().getTokenHash());
        assertTrue(tokenCaptor.getValue().getExpiresAt().isAfter(Instant.now()));
    }

    @Test void resetPasswordHashesPasswordConsumesTokenAndRevokesSessions() {
        String raw = "reset-token"; User user = User.builder().id(UUID.randomUUID()).build();
        PasswordResetToken token = PasswordResetToken.builder().user(user).tokenHash(TokenHashUtils.hash(raw))
                .expiresAt(Instant.now().plusSeconds(60)).build();
        when(tokens.findByTokenHashForUpdate(TokenHashUtils.hash(raw))).thenReturn(Optional.of(token));
        when(encoder.encode("new-password")).thenReturn("new-hash");
        service.resetPassword(raw, "new-password");
        assertEquals("new-hash", user.getPasswordHash());
        assertNotNull(token.getUsedAt());
        verify(refreshTokens).revokeAllByUserId(eq(user.getId()), any(Instant.class));
    }

    @Test void resetPasswordRejectsAlreadyUsedToken() {
        PasswordResetToken token = PasswordResetToken.builder().usedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(30)).build();
        when(tokens.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(token));
        AppException ex = assertThrows(AppException.class, () -> service.resetPassword("used", "new-password"));
        assertEquals(ErrorCode.PASSWORD_RESET_TOKEN_INVALID, ex.getErrorCode());
        verifyNoInteractions(encoder);
    }
}
