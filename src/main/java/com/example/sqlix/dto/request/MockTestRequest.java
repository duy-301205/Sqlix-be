package com.example.sqlix.dto.request;

import com.example.sqlix.enums.DifficultyLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MockTestRequest(
        @NotBlank String title,
        @NotBlank String slug,
        String description,
        @NotNull DifficultyLevel difficulty,
        @NotNull @Min(1) Integer durationMinutes,
        String coverImageUrl) {
}
