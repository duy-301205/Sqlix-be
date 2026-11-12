package com.example.sqlix.dto.request;

import com.example.sqlix.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MockTestQuestionRequest(
        @NotNull QuestionType questionType,
        UUID databaseId,
        @NotBlank String content,
        @NotNull @Min(0) Integer points,
        @NotNull @Min(0) Integer sortOrder,
        List<String> requirements,
        List<String> expectedColumns,
        String solutionQuery,
        @Min(0) Integer maxRetries,
        List<Map<String, Object>> mcOptions,
        String mcCorrectOption,
        String explanation) {
}
