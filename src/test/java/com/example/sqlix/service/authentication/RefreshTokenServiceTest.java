package com.example.sqlix.service.authentication;

import com.example.sqlix.entity.RefreshToken;
import com.example.sqlix.entity.User;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.RefreshTokenRepository;
import com.example.sqlix.utils.TokenHashUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock RefreshTokenRepository repository;
    @InjectMocks RefreshTokenService service;

    @Test void acceptsValidRefreshToken() {
        User user = User.builder().id(UUID.randomUUID()).isActive(true).build();
        RefreshToken token = RefreshToken.builder().user(user).tokenHash(TokenHashUtils.hash("raw"))
                .expiresAt(Instant.now().plusSeconds(60)).build();
        when(repository.findByTokenHashForUpdate(TokenHashUtils.hash("raw"))).thenReturn(Optional.of(token));
        assertSame(token, service.verifyRefreshToken("raw"));
    }

    @Test void rejectsExpiredAndRevokedRefreshTokens() {
        RefreshToken expired = RefreshToken.builder().expiresAt(Instant.now().minusSeconds(1)).build();
        when(repository.findByTokenHashForUpdate(TokenHashUtils.hash("expired"))).thenReturn(Optional.of(expired));
        assertEquals(ErrorCode.REFRESH_TOKEN_EXPIRED,
                assertThrows(AppException.class, () -> service.verifyRefreshToken("expired")).getErrorCode());

        RefreshToken revoked = RefreshToken.builder().revokedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(30)).build();
        when(repository.findByTokenHashForUpdate(TokenHashUtils.hash("revoked"))).thenReturn(Optional.of(revoked));
        assertEquals(ErrorCode.REFRESH_TOKEN_REVOKED,
                assertThrows(AppException.class, () -> service.verifyRefreshToken("revoked")).getErrorCode());
    }

    @Test void logoutRevokesTheOwnedRefreshToken() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        RefreshToken token = RefreshToken.builder().user(user).tokenHash(TokenHashUtils.hash("logout-token")).build();
        when(repository.findByTokenHash(TokenHashUtils.hash("logout-token"))).thenReturn(Optional.of(token));
        service.revokeToken("logout-token", userId);
        assertNotNull(token.getRevokedAt());
        verify(repository).save(token);
    }
}
