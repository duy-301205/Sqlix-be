package com.example.sqlix.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record LessonAdminRequest(
        @NotNull UUID categoryId,
        @NotBlank String title,
        @NotBlank String slug,
        String description,
        List<String> objectives,
        String practiceTopicCode,
        @NotNull @Min(0) Integer sortOrder) {
}
