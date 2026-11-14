package com.example.sqlix.dto.request;

import com.example.sqlix.enums.DifficultyLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MockTestRequest(
        @NotBlank String title,
        @NotBlank String slug,
        String description,
        @NotNull DifficultyLevel difficulty,
        @NotNull @Min(1) Integer durationMinutes,
        String coverImageUrl,
        BigDecimal passingScore,
        Integer maxScore,
        Boolean isPublished) {

    public MockTestRequest(
            String title,
            String slug,
            String description,
            DifficultyLevel difficulty,
            Integer durationMinutes,
            String coverImageUrl) {
        this(title, slug, description, difficulty, durationMinutes, coverImageUrl, BigDecimal.valueOf(7.0), 10, true);
    }
}
