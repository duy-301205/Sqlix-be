package com.example.sqlix.dto.response;

import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.enums.TestSessionStatus;
import java.time.Instant;
import java.util.UUID;

public record MockTestHistoryItemDto(UUID sessionId, UUID testId, String testTitle, DifficultyLevel difficulty,
        Instant submittedAt, int durationSeconds, int correctCount, int totalQuestions, int score,
        boolean isPassed, TestSessionStatus status) {}
