package com.example.sqlix.controller;

import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.request.AdminHistoryFilterRequest;
import com.example.sqlix.enums.TestSessionStatus;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.exception.GlobalExceptionHandler;
import com.example.sqlix.service.mocktest.AdminMockTestHistoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminMockTestHistoryControllerTest {
    private AnnotationConfigApplicationContext context;
    private AdminMockTestHistoryService service;
    private MockMvc mvc;
    @BeforeEach void setup() {
        context=new AnnotationConfigApplicationContext(SecurityTestConfig.class);
        service=context.getBean(AdminMockTestHistoryService.class);
        mvc=MockMvcBuilders.standaloneSetup(context.getBean(AdminMockTestHistoryController.class))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }
    @Test void adminListEndpointReturnsPagedJson() throws Exception {
        when(service.getAllHistories(any())).thenReturn(new PageImpl<>(List.of()));
        mvc.perform(get("/api/admin/mock-tests/history")).andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content").isArray());
    }
    @Test void adminDeleteReturns404ForMissingSession() throws Exception {
        UUID id=UUID.randomUUID(); doThrow(new AppException(ErrorCode.TEST_SESSION_NOT_FOUND)).when(service).adminDeleteSession(id);
        mvc.perform(delete("/api/admin/test-sessions/{sessionId}",id)).andExpect(status().isNotFound());
    }
    @Test void bindsIndependentResultStatusAndOtherFilters() throws Exception {
        when(service.getAllHistories(any())).thenReturn(new PageImpl<>(List.of()));
        String testId = UUID.randomUUID().toString();
        mvc.perform(get("/api/admin/mock-tests/history")
                .param("keyword", "learner@example.com").param("testId", testId)
                .param("isPassed", "false").param("status", "TIMEOUT")
                .param("fromDate", "2026-11-13T17:00:00Z")
                .param("toDate", "2026-11-14T16:59:59.999Z")
                .param("minScore", "0").param("maxScore", "60"))
                .andExpect(status().isOk());
        var captured = ArgumentCaptor.forClass(AdminHistoryFilterRequest.class);
        verify(service).getAllHistories(captured.capture());
        var filter = captured.getValue();
        assertThat(filter.getIsPassed()).isFalse();
        assertThat(filter.getStatus()).isEqualTo(TestSessionStatus.TIMEOUT);
        assertThat(filter.getKeyword()).isEqualTo("learner@example.com");
        assertThat(filter.getTestId()).isEqualTo(UUID.fromString(testId));
        assertThat(filter.getMinScore()).isZero();
        assertThat(filter.getMaxScore()).isEqualTo(60);
        assertThat(filter.getFromDate()).isEqualTo(java.time.Instant.parse("2026-11-13T17:00:00Z"));
        assertThat(filter.getToDate()).isEqualTo(java.time.Instant.parse("2026-11-14T16:59:59.999Z"));
    }
    @Test void nonAdminIsForbiddenByMethodSecurity() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        mvc.perform(get("/api/admin/mock-tests/history")).andExpect(status().isForbidden());
    }
    @AfterEach void cleanup(){SecurityContextHolder.clearContext(); if(context!=null) context.close();}
    @Configuration @EnableMethodSecurity
    static class SecurityTestConfig {
        @Bean AdminMockTestHistoryService adminMockTestHistoryService(){return mock(AdminMockTestHistoryService.class);}
        @Bean AdminMockTestHistoryController adminMockTestHistoryController(AdminMockTestHistoryService s){return new AdminMockTestHistoryController(s);}
    }
}
