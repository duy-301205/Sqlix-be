package com.example.sqlix.dto.response;

import com.example.sqlix.enums.DifficultyLevel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record WorkspaceDto(
        UUID exerciseId,
        UUID databaseId,
        String databaseName,
        String databaseSlug,
        String title,
        String description,
        DifficultyLevel difficulty,
        String topics,
        int maxScore,
        List<String> requirements,
        List<String> expectedColumns,
        Map<String, Object> schemaData,
        String lastSubmittedQuery) {
}
