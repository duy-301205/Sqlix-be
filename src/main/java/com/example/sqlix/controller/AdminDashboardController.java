package com.example.sqlix.controller;

import com.example.sqlix.dto.response.AdminDashboardStatsDto;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.service.dashboard.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    public ApiResponse<AdminDashboardStatsDto> getSystemStats() {
        return ApiResponse.<AdminDashboardStatsDto>builder().code(200)
                .result(adminDashboardService.getSystemStats()).build();
    }
}
