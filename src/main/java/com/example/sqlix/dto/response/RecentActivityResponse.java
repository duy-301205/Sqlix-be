package com.example.sqlix.dto.response;

import com.example.sqlix.enums.ActivityType;

import java.time.Instant;
import java.util.UUID;

public record RecentActivityResponse(
        UUID activityId,
        ActivityType type,
        UUID referenceId,
        Object metaData,
        Instant createdAt) {
}
