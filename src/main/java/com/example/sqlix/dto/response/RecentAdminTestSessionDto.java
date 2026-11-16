package com.example.sqlix.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RecentAdminTestSessionDto(UUID id, String userName, String userEmail,
        String testTitle, int score, int maxScore, boolean isPassed,
        int durationSeconds, Instant submittedAt) {
}
