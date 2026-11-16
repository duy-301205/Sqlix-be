package com.example.sqlix.controller;

import com.example.sqlix.dto.response.QueryResultDto;
import com.example.sqlix.entity.User;
import com.example.sqlix.service.mocktest.MockTestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MockTestRunControllerTest {
    @Mock MockTestService service;
    @InjectMocks MockTestController controller;
    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    @Test void routesPreviewToOwnedSessionAndValidatesQuery() throws Exception {
        UUID userId = UUID.randomUUID(), sessionId = UUID.randomUUID(), questionId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                User.builder().id(userId).build(), null, List.of()));
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(service.runQuery(userId, sessionId, questionId, "SELECT 1"))
                .thenReturn(new QueryResultDto(List.of("value"), List.of(List.of(1)), null));
        String path = "/api/test-sessions/" + sessionId + "/questions/" + questionId + "/run";
        mvc.perform(post(path).contentType("application/json").content("{\"query\":\"SELECT 1\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.rows[0][0]").value(1));
        mvc.perform(post(path).contentType("application/json").content("{\"query\":\" \"}"))
                .andExpect(status().isBadRequest());
        verify(service).runQuery(userId, sessionId, questionId, "SELECT 1");
        verifyNoMoreInteractions(service);
    }
}
