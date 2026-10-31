package com.example.sqlix.dto.response;

import com.example.sqlix.enums.LessonStatus;

import java.util.List;
import java.util.UUID;

public record LessonDetailResponse(
        UUID id,
        String title,
        String slug,
        String description,
        List<String> objectives,
        String practiceTopicCode,
        List<LessonSectionDto> sections,
        boolean isBookmarked,
        LessonStatus status) {
}
