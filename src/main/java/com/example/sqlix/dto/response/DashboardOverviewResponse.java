package com.example.sqlix.dto.response;

public record DashboardOverviewResponse(
        long totalStudyTimeSeconds,
        long totalLessonsCompleted,
        long totalExercisesSolved,
        double averageScore,
        int todayStudyTimeSeconds,
        int dailyTargetMinutes) {
}
