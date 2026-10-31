package com.example.sqlix.controller;

import com.example.sqlix.dto.response.DashboardOverviewResponse;
import com.example.sqlix.dto.response.DailyChartResponse;
import com.example.sqlix.dto.response.RecentActivityResponse;
import com.example.sqlix.dto.response.TopicMasteryResponse;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.SqlTopicCode;
import com.example.sqlix.service.dashboard.DashboardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {
    @Mock DashboardService dashboardService;
    @InjectMocks DashboardController dashboardController;

    private MockMvc mockMvc;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
        userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("dashboard-user").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void overviewReturnsOk() throws Exception {
        when(dashboardService.getOverview(userId)).thenReturn(
                new DashboardOverviewResponse(600, 2, 5, 90.0, 120, 20));

        mockMvc.perform(get("/api/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.totalStudyTimeSeconds").value(600));

        verify(dashboardService).getOverview(userId);
    }

    @Test
    void masteryReturnsOk() throws Exception {
        when(dashboardService.getTopicMasteries(userId)).thenReturn(
                List.of(new TopicMasteryResponse(SqlTopicCode.AGGREGATION, new BigDecimal("75.00"))));

        mockMvc.perform(get("/api/dashboard/mastery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result[0].topicCode").value("AGGREGATION"))
                .andExpect(jsonPath("$.result[0].masteryPercentage").value(75.0));

        verify(dashboardService).getTopicMasteries(userId);
    }

    @Test
    void thirtyDayChartReturnsOk() throws Exception {
        when(dashboardService.get30DayActivityChart(userId)).thenReturn(
                List.of(new DailyChartResponse(java.time.LocalDate.of(2026, 10, 31), 3, 80.0)));

        mockMvc.perform(get("/api/dashboard/chart/30-days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result[0].exercisesSolved").value(3))
                .andExpect(jsonPath("$.result[0].averageScore").value(80.0));

        verify(dashboardService).get30DayActivityChart(userId);
    }

    @Test
    void recentActivitiesReturnsOk() throws Exception {
        when(dashboardService.getRecentActivities(userId)).thenReturn(List.of(new RecentActivityResponse(
                UUID.randomUUID(), com.example.sqlix.enums.ActivityType.EXERCISE_SOLVED,
                UUID.randomUUID(), java.util.Map.of("score", 90), Instant.parse("2026-10-31T10:00:00Z"))));

        mockMvc.perform(get("/api/dashboard/recent-activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result[0].type").value("EXERCISE_SOLVED"))
                .andExpect(jsonPath("$.result[0].metaData.score").value(90));

        verify(dashboardService).getRecentActivities(userId);
    }
}
