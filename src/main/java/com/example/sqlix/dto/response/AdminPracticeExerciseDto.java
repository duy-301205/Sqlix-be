package com.example.sqlix.dto.response;

import com.example.sqlix.enums.DifficultyLevel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminPracticeExerciseDto(
        UUID id,
        UUID databaseId,
        String title,
        String description,
        DifficultyLevel difficulty,
        String topics,
        int maxScore,
        List<String> requirements,
        List<String> expectedColumns,
        String solutionQuery,
        int sortOrder,
        Instant createdAt,
        Instant updatedAt) {
}
