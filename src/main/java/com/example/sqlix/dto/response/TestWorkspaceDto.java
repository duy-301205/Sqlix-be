package com.example.sqlix.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TestWorkspaceDto(
        UUID id,
        Instant startTime,
        int durationMinutes,
        List<WorkspaceQuestionDto> questions) {
}
