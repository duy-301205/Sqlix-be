package com.example.sqlix.controller;

import com.example.sqlix.dto.response.CurrentUserResponse;
import com.example.sqlix.enums.UserOccupation;
import com.example.sqlix.enums.UserSystemRole;
import com.example.sqlix.exception.GlobalExceptionHandler;
import com.example.sqlix.service.user.UserProfileService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock UserProfileService userProfileService;
    @InjectMocks UserController controller;
    MockMvc mvc;

    @BeforeEach void setup() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .addFilters(new TestBearerFilter())
                .build();
    }

    @Test void getMeReturnsUserAndProfileFields() throws Exception {
        when(userProfileService.getCurrentUser("learner@example.com")).thenReturn(CurrentUserResponse.builder()
                .id(UUID.randomUUID()).fullName("Learner").username("learner").email("learner@example.com")
                .avatarUrl("https://example.com/a.png").role(UserSystemRole.USER).occupation(UserOccupation.STUDENT)
                .currentStreak(5).longestStreak(12).dailyTargetMinutes(25).bio("SQL learner").build());
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer test-access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.full_name").value("Learner"))
                .andExpect(jsonPath("$.result.username").value("learner"))
                .andExpect(jsonPath("$.result.email").value("learner@example.com"))
                .andExpect(jsonPath("$.result.avatar_url").value("https://example.com/a.png"))
                .andExpect(jsonPath("$.result.role").value("USER"))
                .andExpect(jsonPath("$.result.current_streak").value(5))
                .andExpect(jsonPath("$.result.longest_streak").value(12))
                .andExpect(jsonPath("$.result.daily_target_minutes").value(25));
        verify(userProfileService).getCurrentUser("learner@example.com");
    }

    @Test void getMeWithoutAccessTokenReturns401() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        verifyNoInteractions(userProfileService);
    }

    @Test void updateProfileReturnsUpdatedData() throws Exception {
        when(userProfileService.updateCurrentUser(eq("learner@example.com"), any())).thenReturn(CurrentUserResponse.builder()
                .id(UUID.randomUUID()).fullName("Learner").username("learner").email("learner@example.com")
                .role(UserSystemRole.USER).occupation(UserOccupation.PROFESSIONAL).currentStreak(2)
                .longestStreak(4).dailyTargetMinutes(40).bio("Updated bio").build());
        mvc.perform(put("/api/users/me").header("Authorization", "Bearer test-access")
                        .contentType("application/json").content("""
                                {"full_name":"Learner","avatar_url":null,"bio":"Updated bio","daily_target_minutes":40,"occupation":"PROFESSIONAL"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.bio").value("Updated bio"))
                .andExpect(jsonPath("$.result.daily_target_minutes").value(40));
    }

    @Test void updateProfileRejectsNonPositiveDailyTarget() throws Exception {
        mvc.perform(put("/api/users/me").header("Authorization", "Bearer test-access")
                        .contentType("application/json").content("""
                                {"full_name":"Learner","bio":"x","daily_target_minutes":-5,"occupation":"STUDENT"}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userProfileService);
    }

    private static class TestBearerFilter extends OncePerRequestFilter {
        @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                                 FilterChain filterChain) throws ServletException, IOException {
            String auth = request.getHeader("Authorization");
            if (request.getRequestURI().startsWith("/api/users/me") && (auth == null || !auth.startsWith("Bearer "))) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            if (auth != null && auth.startsWith("Bearer ")) {
                var authentication = new UsernamePasswordAuthenticationToken("learner@example.com", null,
                        AuthorityUtils.NO_AUTHORITIES);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                HttpServletRequest authenticatedRequest = new HttpServletRequestWrapper(request) {
                    @Override public java.security.Principal getUserPrincipal() { return authentication; }
                };
                try { filterChain.doFilter(authenticatedRequest, response); }
                finally { SecurityContextHolder.clearContext(); }
                return;
            }
            try { filterChain.doFilter(request, response); }
            finally { SecurityContextHolder.clearContext(); }
        }
    }
}
