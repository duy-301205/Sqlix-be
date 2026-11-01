package com.example.sqlix.dto.response;

import com.example.sqlix.enums.DifficultyLevel;

import java.util.UUID;

public record ExerciseSummaryDto(
        UUID id,
        String title,
        DifficultyLevel difficulty,
        String topics,
        int maxScore,
        boolean isPassed) {
}
