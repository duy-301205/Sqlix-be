package com.example.sqlix.dto.response;

import com.example.sqlix.enums.LessonStatus;

import java.util.UUID;

public record LessonSummaryDto(
        UUID id,
        String title,
        String slug,
        int sortOrder,
        LessonStatus status) {
}
