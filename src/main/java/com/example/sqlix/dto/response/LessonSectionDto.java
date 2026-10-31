package com.example.sqlix.dto.response;

import java.util.Map;
import java.util.UUID;

public record LessonSectionDto(
        UUID id,
        String title,
        String content,
        String sampleSql,
        Map<String, Object> noteBox,
        int sortOrder) {
}
