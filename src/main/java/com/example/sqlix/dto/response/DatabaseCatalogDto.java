package com.example.sqlix.dto.response;

import com.example.sqlix.enums.DifficultyLevel;

import java.math.BigDecimal;
import java.util.UUID;

public record DatabaseCatalogDto(
        UUID id,
        String name,
        String slug,
        String description,
        DifficultyLevel difficulty,
        String iconUrl,
        int tablesCount,
        int exercisesCount,
        BigDecimal rating) {
}
