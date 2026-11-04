package com.example.sqlix.controller;

import com.example.sqlix.dto.response.AdminDashboardStatsDto;
import com.example.sqlix.service.dashboard.AdminDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {
    @Mock AdminDashboardService service;
    @InjectMocks AdminDashboardController controller;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void returnsSystemStats() throws Exception {
        when(service.getSystemStats()).thenReturn(new AdminDashboardStatsDto(10, 25, 6, 4));

        mvc.perform(get("/api/admin/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalUsers").value(10))
                .andExpect(jsonPath("$.result.exercisesCompleted").value(25))
                .andExpect(jsonPath("$.result.totalMockTestSessions").value(6));
    }
}
