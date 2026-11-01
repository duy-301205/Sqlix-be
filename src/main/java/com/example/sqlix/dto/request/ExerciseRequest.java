package com.example.sqlix.dto.request;

import com.example.sqlix.enums.DifficultyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ExerciseRequest(
        @NotBlank String title,
        String description,
        @NotNull DifficultyLevel difficulty,
        String topics,
        @NotNull @Min(0) Integer maxScore,
        List<String> requirements,
        List<String> expectedColumns,
        @NotBlank String solutionQuery,
        @NotNull @Min(0) Integer sortOrder) {
}
