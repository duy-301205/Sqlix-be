package com.example.sqlix.dto.response;

public record AdminDashboardStatsDto(
        long totalUsers,
        long exercisesCompleted,
        long totalMockTestSessions,
        long completedMockTestSessions) {
}
