package com.example.sqlix.dto.response;

import com.example.sqlix.enums.QuestionType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AdminMockTestQuestionDto(
        UUID id,
        UUID mockTestId,
        QuestionType questionType,
        UUID databaseId,
        String content,
        Integer points,
        Integer sortOrder,
        List<String> requirements,
        List<String> expectedColumns,
        String solutionQuery,
        Integer maxRetries,
        List<Map<String, Object>> mcOptions,
        String mcCorrectOption,
        String explanation
) {}
