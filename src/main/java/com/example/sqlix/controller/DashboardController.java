package com.example.sqlix.controller;

import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.dto.response.DashboardOverviewResponse;
import com.example.sqlix.dto.response.DailyChartResponse;
import com.example.sqlix.dto.response.RecentActivityResponse;
import com.example.sqlix.dto.response.TopicMasteryResponse;
import com.example.sqlix.entity.User;
import com.example.sqlix.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewResponse> getOverview() {
        UUID userId = currentUserId();
        return ApiResponse.<DashboardOverviewResponse>builder()
                .code(200)
                .result(dashboardService.getOverview(userId))
                .build();
    }

    @GetMapping("/mastery")
    public ApiResponse<List<TopicMasteryResponse>> getTopicMasteries() {
        UUID userId = currentUserId();
        return ApiResponse.<List<TopicMasteryResponse>>builder()
                .code(200)
                .result(dashboardService.getTopicMasteries(userId))
                .build();
    }

    @GetMapping("/chart/30-days")
    public ApiResponse<List<DailyChartResponse>> get30DayActivityChart() {
        return ApiResponse.<List<DailyChartResponse>>builder()
                .code(200)
                .result(dashboardService.get30DayActivityChart(currentUserId()))
                .build();
    }

    @GetMapping("/recent-activities")
    public ApiResponse<List<RecentActivityResponse>> getRecentActivities() {
        return ApiResponse.<List<RecentActivityResponse>>builder()
                .code(200)
                .result(dashboardService.getRecentActivities(currentUserId()))
                .build();
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((User) authentication.getPrincipal()).getId();
    }
}
