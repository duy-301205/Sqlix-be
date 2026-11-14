package com.example.sqlix.dto.response;

import com.example.sqlix.enums.DifficultyLevel;

import java.util.UUID;

public record MockTestCatalogDto(
        UUID id,
        String title,
        String slug,
        String description,
        DifficultyLevel difficulty,
        int durationMinutes,
        int totalQuestions,
        String coverImageUrl,
        int totalParticipants,
        Double passingScore,
        Integer maxScore,
        Boolean isPublished) {
}
