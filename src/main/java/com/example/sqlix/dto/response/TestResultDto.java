package com.example.sqlix.dto.response;

import com.example.sqlix.enums.TestSessionStatus;

import java.util.List;
import java.util.UUID;

public record TestResultDto(
        UUID sessionId,
        int totalScore,
        TestSessionStatus status,
        long durationDeducted,
        List<QuestionResultDto> questions) {
}
