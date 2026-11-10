package com.example.sqlix.dto.response;

import com.example.sqlix.enums.DifficultyLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AdminPracticeDatabaseDto(
        UUID id,
        String name,
        String slug,
        String description,
        DifficultyLevel difficulty,
        String iconUrl,
        Map<String, Object> schemaData,
        String initSqlScript,
        int tablesCount,
        int exercisesCount,
        BigDecimal rating,
        Instant createdAt,
        Instant updatedAt) {
}
