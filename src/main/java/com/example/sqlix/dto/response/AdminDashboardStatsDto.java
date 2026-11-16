package com.example.sqlix.dto.response;

public record AdminDashboardStatsDto(
        long totalUsers,
        long exercisesCompleted,
        long totalMockTestSessions,
        long completedMockTestSessions,
        java.util.List<RecentAdminTestSessionDto> recentTestSessions,
        long totalTestSessions,
        double averageTestScore,
        double passRatePercent) {
    public AdminDashboardStatsDto(long totalUsers, long exercisesCompleted,
            long totalMockTestSessions, long completedMockTestSessions) {
        this(totalUsers, exercisesCompleted, totalMockTestSessions, completedMockTestSessions, java.util.List.of(),
                completedMockTestSessions, 0, 0);
    }
}
