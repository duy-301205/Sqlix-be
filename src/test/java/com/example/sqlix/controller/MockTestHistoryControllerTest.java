package com.example.sqlix.controller;

import com.example.sqlix.dto.response.MockTestHistorySummaryDto;
import com.example.sqlix.entity.User;
import com.example.sqlix.service.mocktest.MockTestHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MockTestHistoryControllerTest {
    private MockTestHistoryService service;
    private MockMvc mvc;
    private UUID userId;
    @BeforeEach void setup() {
        service=mock(MockTestHistoryService.class); userId=UUID.randomUUID();
        mvc=MockMvcBuilders.standaloneSetup(new MockTestHistoryController(service)).build();
        User user=new User(); user.setId(userId);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null));
    }
    @Test void summaryEndpointReturnsExpectedEnvelopeAndMetrics() throws Exception {
        when(service.getUserHistorySummary(userId)).thenReturn(new MockTestHistorySummaryDto(3,70.5,92,66.67));
        mvc.perform(get("/api/mock-tests/history/summary")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)).andExpect(jsonPath("$.result.totalTestsTaken").value(3))
                .andExpect(jsonPath("$.result.averageScore").value(70.5)).andExpect(jsonPath("$.result.highestScore").value(92))
                .andExpect(jsonPath("$.result.passRatePercentage").value(66.67));
    }
    @Test void historyEndpointReturnsPagedStructure() throws Exception {
        when(service.getUserHistoryList(eq(userId),any())).thenReturn(new PageImpl<>(List.of()));
        mvc.perform(get("/api/mock-tests/history").param("page","0").param("size","10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.content").isArray()).andExpect(jsonPath("$.result.totalElements").value(0));
    }
    @org.junit.jupiter.api.AfterEach void cleanup(){SecurityContextHolder.clearContext();}
}
