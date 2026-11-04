package com.example.sqlix.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CourseCategoryRequest(
        @NotBlank String name,
        String description,
        @Min(0) Integer sortOrder) {
}
