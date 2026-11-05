package com.example.sqlix.dto.response;

import com.example.sqlix.enums.TestSessionStatus;
import java.time.Instant;
import java.util.UUID;

public record AdminHistoryItemDto(UUID sessionId, UUID userId, String username, String fullName,
        String testTitle, int score, Instant submittedAt, int durationSeconds,
        TestSessionStatus status, boolean isPassed) {}
