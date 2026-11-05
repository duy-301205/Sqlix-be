package com.example.sqlix.dto.response;

import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.enums.TestSessionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MockTestHistoryDetailDto(UUID sessionId, UUID testId, String testTitle, DifficultyLevel difficulty,
        Instant submittedAt, int durationSeconds, int score, boolean isPassed, TestSessionStatus status,
        List<QuestionDetail> questions) {
    public record QuestionDetail(UUID questionId, String questionType, String content, String userAnswer,
            String correctAnswer, String explanation, int score, int maxScore, boolean isCorrect) {}
}
