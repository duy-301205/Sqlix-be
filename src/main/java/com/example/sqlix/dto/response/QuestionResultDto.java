package com.example.sqlix.dto.response;

import com.example.sqlix.enums.QuestionType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record QuestionResultDto(
        UUID questionId,
        QuestionType questionType,
        String content,
        int points,
        int sortOrder,
        List<String> requirements,
        List<String> expectedColumns,
        List<Map<String, Object>> mcOptions,
        UUID databaseId,
        Map<String, Object> schemaData,
        String submittedAnswer,
        int score,
        boolean isCorrect,
        String solutionQuery,
        String mcCorrectOption,
        String explanation) {
}
