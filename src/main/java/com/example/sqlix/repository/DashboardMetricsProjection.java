package com.example.sqlix.repository;

public interface DashboardMetricsProjection {
    Long getTotalStudyTimeSeconds();

    Long getTotalLessonsCompleted();

    Long getTotalExercisesSolved();

    Double getAverageScore();
}
