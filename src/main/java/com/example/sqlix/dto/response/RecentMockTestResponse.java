package com.example.sqlix.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RecentMockTestResponse(UUID sessionId, String title, Instant submittedAt,
        double score, int durationSeconds, int questionCount, boolean isPassed) {}
