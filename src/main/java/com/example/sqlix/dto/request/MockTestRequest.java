package com.example.sqlix.dto.request;

import com.example.sqlix.enums.DifficultyLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MockTestRequest(
        @NotBlank String title,
        @NotBlank String slug,
        String description,
        @NotNull DifficultyLevel difficulty,
        @NotNull @Min(1) Integer durationMinutes,
        String coverImageUrl) {
}
