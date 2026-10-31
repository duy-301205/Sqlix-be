package com.example.sqlix.dto.response;

import java.util.List;
import java.util.UUID;

public record CurriculumCategoryDto(
        UUID id,
        String name,
        String description,
        int sortOrder,
        List<LessonSummaryDto> lessons) {
}
