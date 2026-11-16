package com.example.sqlix.repository;

public interface FinishedTestStatsProjection {
    long getFinishedCount();
    long getPassedCount();
    double getAverageScore();
}
