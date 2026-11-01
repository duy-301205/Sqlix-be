package com.example.sqlix.dto.response;

public record AnswerResponseDto(boolean isCorrect, int score, int retryCount, String feedback) {
}
