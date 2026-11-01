package com.example.sqlix.dto.response;

import com.example.sqlix.enums.DifficultyLevel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DatabaseDetailDto(
        UUID id,
        String name,
        String slug,
        String description,
        DifficultyLevel difficulty,
        String iconUrl,
        Map<String, Object> schemaData,
        int tablesCount,
        int exercisesCount,
        BigDecimal rating,
        List<ExerciseSummaryDto> exercises) {
}
