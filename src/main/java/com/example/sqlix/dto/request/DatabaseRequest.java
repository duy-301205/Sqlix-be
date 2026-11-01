package com.example.sqlix.dto.request;

import com.example.sqlix.enums.DifficultyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record DatabaseRequest(
        @NotBlank String name,
        @NotBlank String slug,
        String description,
        @NotNull DifficultyLevel difficulty,
        String iconUrl,
        @NotNull Map<String, Object> schemaData,
        @NotBlank String initSqlScript,
        @NotNull @Min(0) Integer tablesCount,
        @NotNull @Min(0) Integer exercisesCount) {
}
