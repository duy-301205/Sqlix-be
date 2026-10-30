package com.example.sqlix.service.authentication;

import com.example.sqlix.dto.request.LoginRequest;
import com.example.sqlix.dto.request.RegisterRequest;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.UserOccupation;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock UserRepository users;
    @Mock RefreshTokenService refreshTokens;
    @Mock EmailVerificationService verification;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @InjectMocks AuthenticationService service;

    @Test void registerHashesPasswordAndCreatesUser() {
        RegisterRequest request = RegisterRequest.builder().username("learner").email("A@EXAMPLE.COM")
                .password("Secret123").fullName("Learner One").occupation(UserOccupation.STUDENT).termsAccepted(true).build();
        when(encoder.encode("Secret123")).thenReturn("bcrypt-hash");
        when(users.saveAndFlush(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0); u.setId(UUID.randomUUID()); return u;
        });
        var result = service.register(request);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).saveAndFlush(captor.capture());
        assertEquals("bcrypt-hash", captor.getValue().getPasswordHash());
        assertEquals("a@example.com", captor.getValue().getEmail());
        verify(verification).sendInitialVerification(any(User.class));
        assertNotNull(result.getId());
    }

    @Test void registerMapsUniqueConstraintFailureToFriendlyBadRequest() {
        RegisterRequest request = RegisterRequest.builder().username("learner").email("a@example.com")
                .password("Secret123").fullName("Learner").occupation(UserOccupation.STUDENT).termsAccepted(true).build();
        when(encoder.encode(anyString())).thenReturn("hash");
        when(users.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique"));
        AppException ex = assertThrows(AppException.class, () -> service.register(request));
        assertEquals(ErrorCode.DATA_CONSTRAINT_VIOLATION, ex.getErrorCode());
        assertEquals(400, ex.getErrorCode().getStatusCode().value());
    }

    @Test void loginAcceptsVerifiedUserAndRejectsUnverifiedUser() {
        User user = User.builder().id(UUID.randomUUID()).emailVerified(true).isActive(true).passwordHash("hash").build();
        when(users.findByUsernameOrEmail("learner", "learner")).thenReturn(Optional.of(user));
        when(encoder.matches("secret", "hash")).thenReturn(true);
        when(jwt.generateAccessToken(user)).thenReturn("access");
        when(refreshTokens.createRefreshToken(user, "agent", "ip")).thenReturn("refresh");
        assertEquals("access", service.login(loginRequest(), "agent", "ip").getAccessToken());

        user.setEmailVerified(false);
        AppException ex = assertThrows(AppException.class, () -> service.login(loginRequest(), "agent", "ip"));
        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, ex.getErrorCode());
        verify(encoder, times(1)).matches("secret", "hash");
    }

    private LoginRequest loginRequest() {
        LoginRequest req = new LoginRequest(); req.setIdentifier("learner"); req.setPassword("secret"); return req;
    }
}
