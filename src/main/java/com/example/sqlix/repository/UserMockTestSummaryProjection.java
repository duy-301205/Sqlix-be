package com.example.sqlix.repository;

public interface UserMockTestSummaryProjection {
    Long getTotalTestsTaken();
    Double getAverageScore();
    Integer getHighestScore();
    Double getPassRatePercentage();
}
