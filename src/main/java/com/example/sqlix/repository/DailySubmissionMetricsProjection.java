package com.example.sqlix.repository;

import java.time.LocalDate;

public interface DailySubmissionMetricsProjection {
    LocalDate getDate();
    Integer getExercisesSolved();
    Double getAverageScore();
}
