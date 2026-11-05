package com.example.sqlix.dto.response;

import java.time.Instant;

public record ProgressTrendDto(Instant submittedAt, int score, String testTitle, boolean isPassed) {}
