package com.example.sqlix.dto.response;

import com.example.sqlix.enums.TestSessionStatus;

import java.time.Instant;
import java.util.UUID;

public record SessionStartResponse(
        UUID sessionId,
        Instant startTime,
        int durationMinutes,
        TestSessionStatus status) {
}
