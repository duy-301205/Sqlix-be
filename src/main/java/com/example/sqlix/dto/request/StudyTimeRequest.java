package com.example.sqlix.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record StudyTimeRequest(@Min(1) @Max(60) int seconds) {
}
