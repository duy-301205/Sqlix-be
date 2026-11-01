package com.example.sqlix.dto.response;

import com.example.sqlix.enums.SubmissionStatus;

public record SubmissionResultDto(
        SubmissionStatus status,
        int score,
        String feedback,
        QueryResultDto actualResult) {
}
