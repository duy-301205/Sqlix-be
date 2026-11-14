package com.example.sqlix.dto.response;

import com.example.sqlix.enums.TestSessionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TestResultDto(
        UUID sessionId,
        int totalScore,
        Integer maxScore,
        BigDecimal passingScore,
        boolean isPassed,
        TestSessionStatus status,
        long durationDeducted,
        List<QuestionResultDto> questions) {
}
