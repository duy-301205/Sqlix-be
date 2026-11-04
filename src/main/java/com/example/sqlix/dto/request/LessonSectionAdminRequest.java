package com.example.sqlix.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record LessonSectionAdminRequest(
        @NotBlank String title,
        @NotBlank String content,
        String sampleSql,
        Map<String, Object> noteBox,
        @NotNull @Min(0) Integer sortOrder) {
}
