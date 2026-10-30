package com.example.sqlix.controller;

import com.example.sqlix.dto.response.RegisterResponse;
import com.example.sqlix.enums.UserOccupation;
import com.example.sqlix.enums.UserSystemRole;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.exception.GlobalExceptionHandler;
import com.example.sqlix.service.authentication.AuthenticationService;
import com.example.sqlix.service.authentication.EmailVerificationService;
import com.example.sqlix.service.authentication.OAuthAuthenticationService;
import com.example.sqlix.service.authentication.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {
    @Mock AuthenticationService authenticationService;
    @Mock EmailVerificationService emailVerificationService;
    @Mock PasswordService passwordService;
    @Mock OAuthAuthenticationService oauthAuthenticationService;
    @InjectMocks AuthenticationController controller;
    MockMvc mvc;
    LocalValidatorFactoryBean validator;

    @BeforeEach void setup() {
        validator = new LocalValidatorFactoryBean(); validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator).build();
    }

    @Test void registerReturnsHttp200AndResponseBody() throws Exception {
        when(authenticationService.register(any())).thenReturn(RegisterResponse.builder().id(UUID.randomUUID())
                .username("learner").email("learner@example.com").fullName("Learner")
                .occupation(UserOccupation.STUDENT).systemRole(UserSystemRole.USER).createdAt(Instant.now()).build());
        mvc.perform(post("/api/auth/register").contentType("application/json").content("""
                {"username":"learner","email":"learner@example.com","password":"Secret123","fullName":"Learner","occupation":"STUDENT","termsAccepted":true}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.username").value("learner"));
    }

    @Test void registerConstraintErrorReturnsHttp400AndFriendlyMessage() throws Exception {
        when(authenticationService.register(any())).thenThrow(new AppException(ErrorCode.DATA_CONSTRAINT_VIOLATION));
        mvc.perform(post("/api/auth/register").contentType("application/json").content("""
                {"username":"learner","email":"learner@example.com","password":"Secret123","fullName":"Learner","occupation":"STUDENT","termsAccepted":true}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.DATA_CONSTRAINT_VIOLATION.getMessage()));
    }

    @Test void forgotPasswordReturnsEnumerationSafeResponse() throws Exception {
        mvc.perform(post("/api/auth/forgot-password").contentType("application/json")
                .content("{\"email\":\"learner@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").exists());
        verify(passwordService).requestPasswordReset("learner@example.com");
    }

    @Test void logoutRevokesBodyTokenAndReturns200() throws Exception {
        var principal = new UsernamePasswordAuthenticationToken("learner@example.com", null, AuthorityUtils.NO_AUTHORITIES);
        mvc.perform(post("/api/auth/logout").principal(principal).contentType("application/json")
                        .content("{\"refresh_token\":\"opaque-refresh\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Logout successful"));
        verify(authenticationService).logout(argThat(request -> "opaque-refresh".equals(request.getRefreshToken())),
                eq("learner@example.com"));
    }

    @Test void logoutAcceptsCookieAndExpiresIt() throws Exception {
        var principal = new UsernamePasswordAuthenticationToken("learner@example.com", null, AuthorityUtils.NO_AUTHORITIES);
        mvc.perform(post("/api/auth/logout").principal(principal).cookie(new jakarta.servlet.http.Cookie("refresh_token", "cookie-refresh")))
                .andExpect(status().isOk()).andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));
        verify(authenticationService).logout(argThat(request -> "cookie-refresh".equals(request.getRefreshToken())),
                eq("learner@example.com"));
    }
}
