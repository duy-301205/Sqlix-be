package com.example.sqlix.dto.request;

import jakarta.validation.constraints.NotBlank;

public record QueryRequestDto(@NotBlank String query) {
}
