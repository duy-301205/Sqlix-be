package com.example.sqlix.dto.response;

public record DashboardOverviewResponse(
        long totalStudyTimeSeconds,
        long totalLessonsCompleted,
        long totalExercisesSolved,
        double averageScore,
        int todayStudyTimeSeconds,
        int dailyTargetMinutes,
        long totalLessons,
        long totalExercises,
        RecentMockTestResponse recentMockTest) {
}
