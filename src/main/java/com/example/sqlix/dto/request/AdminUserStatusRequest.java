package com.example.sqlix.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminUserStatusRequest(@NotNull Boolean active) {
}
