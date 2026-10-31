package com.example.sqlix.dto.response;

import java.time.LocalDate;

public record DailyChartResponse(
        LocalDate date,
        int exercisesSolved,
        double averageScore) {
}
